package com.balex.familyteam.presentation.regadmin

import android.util.Log
import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.TIMEOUT_VERIFICATION_PHONE
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.domain.usecase.regLog.EmitUserNeedRefreshUseCase
import com.balex.familyteam.domain.usecase.regLog.RegUserWithFakeEmailUseCase
import com.balex.familyteam.domain.usecase.regLog.RegUserWithFakeEmailToAuthAndToUsersCollectionUseCase
import com.balex.familyteam.domain.usecase.regLog.SetUserAsVerifiedUseCase
import com.balex.familyteam.presentation.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PhoneFirebaseRepositoryImpl @Inject constructor(
    private val regUserWithFakeEmailToAuthAndToUsersCollectionUseCase: RegUserWithFakeEmailToAuthAndToUsersCollectionUseCase,
    private val regUserWithFakeEmailUseCase: RegUserWithFakeEmailUseCase,
    private val setUserAsVerifiedUseCase: SetUserAsVerifiedUseCase,
    private val emitUserNeedRefreshUseCase: EmitUserNeedRefreshUseCase
) : PhoneFirebaseRepository {

    private var storedSmsVerificationId = SMS_VERIFICATION_ID_INITIAL
    private var resendTokenForSmsVerification: PhoneAuthProvider.ForceResendingToken? = null
    private var isUserMailOrPhoneVerified = false



    override suspend fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    ) {
        val auth = Firebase.auth

        try {
            val verificationId = suspendCancellableCoroutine<String> { continuation ->
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(TIMEOUT_VERIFICATION_PHONE, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            CoroutineScope(Dispatchers.Default).launch {
                                registerAndSignInWithCredential(
                                    credential,
                                    phoneNumber,
                                    nickName,
                                    displayName,
                                    password
                                )
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            continuation.resumeWithException(e)
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            storedSmsVerificationId = verificationId
                            resendTokenForSmsVerification = token
                            continuation.resume(verificationId)
                        }
                    })
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }

            Log.d("sendSmsVerifyCode", "Code sent successfully: $verificationId")

        } catch (e: Exception) {
            Log.e("sendSmsVerifyCode", "Error: ${e.message}")
        }
    }

    override suspend fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    ) {
        val token = resendTokenForSmsVerification
            ?: throw RuntimeException("resendTokenForSmsVerification is null")

        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(TIMEOUT_VERIFICATION_PHONE, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            CoroutineScope(Dispatchers.Default).launch {
                                registerAndSignInWithCredential(
                                    credential,
                                    phoneNumber,
                                    nickName,
                                    displayName,
                                    password
                                )
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            continuation.resumeWithException(e)
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            storedSmsVerificationId = verificationId
                            resendTokenForSmsVerification = token
                            continuation.resume(Unit)
                        }
                    })
                    .setForceResendingToken(token)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            }

            Log.d("resendVerificationCode", "Code resent successfully")

        } catch (e: Exception) {
            Log.e("resendVerificationCode", "Error: ${e.message}")
        }
    }

    override suspend fun verifySmsCode(
        verificationCode: String,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val credential = PhoneAuthProvider.getCredential(storedSmsVerificationId, verificationCode)
        registerAndSignInWithCredential(credential, phoneNumber, nickName, displayName, password)
    }

    private suspend fun registerAndSignInWithCredential(
        credential: PhoneAuthCredential,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val auth = Firebase.auth


        regUserWithFakeEmailToAuthAndToUsersCollectionUseCase(phoneNumber, nickName, displayName, password)

        try {

            val result = auth.signInWithCredential(credential).await()

            val user = result.user
            if (user != null) {
                while (!isUserMailOrPhoneVerified) {
                    user.reload().await()

                    if (user.phoneNumber == phoneNumber) {
                        regUserWithFakeEmailUseCase(phoneNumber, nickName, displayName, password)
                        setUserAsVerifiedUseCase()
                        emitUserNeedRefreshUseCase()
                        isUserMailOrPhoneVerified = true
                    }

                    delay(1000)
                }
            }

        } catch (e: Exception) {
            Log.e("signInWithCredential", "Error: ${e.message}")
        }
    }

}