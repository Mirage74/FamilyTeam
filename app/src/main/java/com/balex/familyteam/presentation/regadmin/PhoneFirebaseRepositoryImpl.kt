package com.balex.familyteam.presentation.regadmin

import android.util.Log
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.User
import com.balex.familyteam.domain.repository.PhoneFirebaseRepository
import com.balex.common.domain.usecases.regLog.EmitUserNeedRefreshUseCase
import com.balex.common.domain.usecases.regLog.FindAdminInCollectionByDocumentNameUseCase
import com.balex.common.domain.usecases.regLog.RegUserWithFakeEmailToAuthAndToUsersCollectionUseCase
import com.balex.common.domain.usecases.regLog.RemoveRecordFromCollectionUseCase
import com.balex.common.domain.usecases.regLog.SetUserAsVerifiedUseCase
import com.balex.common.domain.usecases.regLog.SetUserWithErrorUseCase
import com.balex.common.domain.usecases.regLog.SetWrongPasswordUserUseCase
import com.balex.common.domain.usecases.regLog.SignToFirebaseWithFakeEmailUseCase
import com.balex.common.extensions.formatStringPhoneDelLeadNullAndAddPlus
import com.balex.familyteam.presentation.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
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
    private val setUserAsVerifiedUseCase: SetUserAsVerifiedUseCase,
    private val setUserWithErrorUseCase: SetUserWithErrorUseCase,
    private val setWrongPasswordUserUseCase: SetWrongPasswordUserUseCase,
    private val emitUserNeedRefreshUseCase: EmitUserNeedRefreshUseCase,
    private val findAdminInCollectionByDocumentNameUseCase: FindAdminInCollectionByDocumentNameUseCase,
    private val removeRecordFromCollection: RemoveRecordFromCollectionUseCase,
    private val signToFirebaseWithFakeEmailUseCase: SignToFirebaseWithFakeEmailUseCase

) : PhoneFirebaseRepository {

    private val db = Firebase.firestore
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)


    private var storedSmsVerificationId = SMS_VERIFICATION_ID_INITIAL
    private var resendTokenForSmsVerification: PhoneAuthProvider.ForceResendingToken? = null


    override suspend fun sendSmsVerifyCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: MainActivity
    ) {

        val isEnteredTeamIsNotRegisteredInFirebase = findAdminInCollectionByDocumentNameUseCase(phoneNumber.formatStringPhoneDelLeadNullAndAddPlus())
        if (isEnteredTeamIsNotRegisteredInFirebase == null) {


            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
            }

            val auth = Firebase.auth

            try {
                val verificationId = suspendCancellableCoroutine { continuation ->
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(TIMEOUT_VERIFICATION_PHONE, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(object :
                            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                //called only when verification without user action
                                CoroutineScope(Dispatchers.Default).launch {
                                    try {
                                        registerAndSignInWithCredential(
                                            credential,
                                            phoneNumber,
                                            nickName,
                                            displayName,
                                            password
                                        )
                                    } catch (e: Exception) {
                                        Log.e("sendSmsVerifyCode", "Error: ${e.message}")
                                        setUserWithErrorUseCase("sendSmsVerifyCode, registerAndSignInWithCredential, error: ${e.message}")
                                    }
                                }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                continuation.resumeWithException(e)
                            }

                            override fun onCodeSent(
                                verifId: String,
                                token: PhoneAuthProvider.ForceResendingToken
                            ) {
                                storedSmsVerificationId = verifId
                                resendTokenForSmsVerification = token
                                continuation.resume(verifId)
                            }
                        })
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }

                //Log.d("sendSmsVerifyCode", "Code sent successfully: $verificationId")

            } catch (e: Exception) {
                Log.e("sendSmsVerifyCode", "Error: ${e.message}")
                setUserWithErrorUseCase("sendSmsVerifyCode, PhoneAuthProvider.verifyPhoneNumber(options), error: ${e.message}")
            }
        } else {
            val phoneWithoutPlus =  if (phoneNumber.startsWith("+")) {
                phoneNumber.substring(1)
            } else {
                phoneNumber
            }
            setWrongPasswordUserUseCase(User(adminEmailOrPhone = phoneWithoutPlus))
        }
    }

    override suspend fun resendVerificationCode(
        phoneNumber: String,
        nickName: String,
        displayName: String,
        password: String,
        activity: com.balex.familyteam.presentation.MainActivity
    ) {
        val token = resendTokenForSmsVerification
            ?: throw RuntimeException("resendTokenForSmsVerification is null")

        try {
            suspendCancellableCoroutine { continuation ->
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

            //Log.d("resendVerificationCode", "Code resent successfully")

        } catch (e: Exception) {
            Log.e("resendVerificationCode", "Error: ${e.message}")
            setUserWithErrorUseCase("resendVerificationCode, PhoneAuthProvider.verifyPhoneNumber(options), error: ${e.message}")
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


        if (isNewTeamCheckInCollections(
                phoneNumber,
                nickName,
                password
            ) == NO_ADMIN_IN_COLLECTION_FOUND_BY_PHONE
        ) {


            regUserWithFakeEmailToAuthAndToUsersCollectionUseCase(
                phoneNumber,
                nickName,
                displayName,
                password
            )

            try {
                var isUserMailOrPhoneVerified = false
                val result = auth.signInWithCredential(credential).await()

                val user = result.user
                if (user != null) {
                    while (!isUserMailOrPhoneVerified) {
                        user.reload().await()

                        if (user.phoneNumber == phoneNumber) {
                            regUserWithFakeEmailToAuthAndToUsersCollectionUseCase(
                                phoneNumber,
                                nickName,
                                displayName,
                                password
                            )
                            setUserAsVerifiedUseCase()
                            emitUserNeedRefreshUseCase()
                            isUserMailOrPhoneVerified = true
                        }

                        delay(TIME_CHECK_IS_USER_VERIFIED_IN_MILLIS)
                    }
                }

            } catch (e: Exception) {
//                Log.e("signInWithCredential", "Error: ${e.message}")
//                setUserWithErrorUseCase("registerAndSignInWithCredential, auth.signInWithCredential, error: ${e.message}")

            }
        }
    }

    private suspend fun findUserAsAdminInCollection(admin: Admin): User? {

        try {


            val adminUserRef =
                usersCollection.document(admin.emailOrPhoneNumber).collection(admin.nickName)

            val querySnapshot = adminUserRef
                .whereEqualTo("admin", true)
                .whereEqualTo("adminEmailOrPhone", admin.emailOrPhoneNumber)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()

            val userData = querySnapshot?.toObject(User::class.java)
            return userData

        } catch (e: Exception) {
            return null
        }

    }


    private suspend fun isNewTeamCheckInCollections(
        phone: String,
        nickName: String,
        password: String
    ): String {
        val admin = findAdminInCollectionByDocumentNameUseCase(phone)

        if (admin == null) {
            removeRecordFromCollection(FIREBASE_USERS_COLLECTION, phone, nickName)
            return NO_ADMIN_IN_COLLECTION_FOUND_BY_PHONE
        } else {
            val adminAsUser = findUserAsAdminInCollection(admin)

            if (adminAsUser == null) {
                removeRecordFromCollection(FIREBASE_ADMINS_COLLECTION, phone, nickName)
                return NO_ADMIN_IN_COLLECTION_FOUND_BY_PHONE
            } else {
                if (adminAsUser.password != password) {
                    return ADMIN_IS_FOUND_IN_COLLECTION_BUT_PASSWORD_IS_WRONG
                } else {
                    try {
                        signToFirebaseWithFakeEmailUseCase(adminAsUser)

                        return adminAsUser.nickName

                    } catch (e: Exception) {
                        e.printStackTrace()
                        setUserWithErrorUseCase("isNewTeamCheckInCollections, signToFirebaseWithFakeEmailUseCase, error: ${e.message}")
                        return e.message ?: "Unknown error"
                    }
                }
            }
        }
    }


    companion object {
        const val SMS_VERIFICATION_ID_INITIAL = "SMS_VERIFICATION_ID_INITIAL"
        const val TIMEOUT_VERIFICATION_PHONE = 60L
        const val TIME_CHECK_IS_USER_VERIFIED_IN_MILLIS = 1000L
        const val NO_ADMIN_IN_COLLECTION_FOUND_BY_PHONE = "NO ADMIN_IN_COLLECTION_FOUND_BY_PHONE"
        const val ADMIN_IS_FOUND_IN_COLLECTION_BUT_PASSWORD_IS_WRONG =
            "ADMIN_IS_FOUND_IN_COLLECTION_BUT_PASSWORD_IS_WRONG"
    }
}