package com.balex.familyteam.presentation.regadmin

import android.util.Log
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.familyteam.presentation.MainActivity
import com.google.firebase.auth.PhoneAuthProvider
import com.balex.familyteam.data.repository.RegLogRepositoryImpl.Companion.TIMEOUT_VERIFICATION
import com.balex.familyteam.domain.usecase.regLog.EmitUserNeedRefreshUseCase
import com.balex.familyteam.domain.usecase.regLog.RegUserWithFakeEmailUseCase
import com.balex.familyteam.domain.usecase.regLog.SetAdminAndUserUseCase
import com.balex.familyteam.domain.usecase.regLog.SetUserAsVerifiedUseCase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhoneFirebaseRepositoryImpl @Inject constructor(
    private val setAdminAndUserUseCase: SetAdminAndUserUseCase,
    private val regUserWithFakeEmailUseCase: RegUserWithFakeEmailUseCase,
    private val setUserAsVerifiedUseCase: SetUserAsVerifiedUseCase,
    private val emitUserNeedRefreshUseCase: EmitUserNeedRefreshUseCase
) : PhoneFirebaseRepository {

    private var storedSmsVerificationId = SMS_VERIFICATION_ID_INITIAL
    private var resendTokenForSmsVerification: PhoneAuthProvider.ForceResendingToken? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var isUserMailOrPhoneVerified = false

    override fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    ) {
        val auth = Firebase.auth
        //val coroutineScope = CoroutineScope(Dispatchers.Main)

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(TIMEOUT_VERIFICATION, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, phoneNumber, nickName, displayName, password)
                }

                override fun onVerificationFailed(e: FirebaseException) {
//                _isSmsVerificationError = e.message ?: "Error"
//                coroutineScope.launch {
//                    isSmsVerificationErrorNeedRefreshFlow.emit(Unit)
//                }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedSmsVerificationId = verificationId
                    resendTokenForSmsVerification = token
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    ) {
        val token: PhoneAuthProvider.ForceResendingToken
        if (resendTokenForSmsVerification != null) {
            token = resendTokenForSmsVerification as PhoneAuthProvider.ForceResendingToken
        } else {
            throw RuntimeException("resendTokenForSmsVerification is null")
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(TIMEOUT_VERIFICATION, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, phoneNumber, nickName, displayName, password)
                }

                override fun onVerificationFailed(e: FirebaseException) {
//                    _isSmsVerificationError = e.message ?: "Error"
//                    coroutineScope.launch {
//                        isSmsVerificationErrorNeedRefreshFlow.emit(Unit)
//                    }
                }

            })
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verifySmsCode(
        verificationCode: String,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String
    ) {
        val credential = PhoneAuthProvider.getCredential(storedSmsVerificationId, verificationCode)
        signInWithCredential(credential, phoneNumber, nickName, displayName, password)
    }

    private fun signInWithCredential(

        credential: PhoneAuthCredential,
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String

    ) {
        val auth = Firebase.auth


        setAdminAndUserUseCase(phoneNumber, nickName, displayName, password)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    coroutineScope.launch {
                        while (!isUserMailOrPhoneVerified) {
                            user?.let {
                                it.reload().addOnCompleteListener { reloadTask ->
                                    if (reloadTask.isSuccessful) {
                                        if (user.phoneNumber == phoneNumber) {
                                            regUserWithFakeEmailUseCase(
                                                phoneNumber,
                                                nickName,
                                                displayName,
                                                password
                                            )
                                            setUserAsVerifiedUseCase()
                                            emitUserNeedRefreshUseCase
                                            isUserMailOrPhoneVerified = true
                                        }
                                    }
                                }
                            }
                            delay(1000)
                        }
                    }
                } else {
                    task.exception?.message?.let {
                        Log.e("Registration Error", it)
                    }
                }
            }
    }

}