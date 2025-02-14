package com.balex.common.data.repository

import android.content.Context
import com.balex.common.R
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.DeletedAdmin
import com.balex.common.domain.entity.DeletedSubUser
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.AdminRepository
import com.balex.common.domain.usecases.regLog.CreateFakeUserEmailUseCase
import com.balex.common.domain.usecases.regLog.GetRepoAdminUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.regLog.LogoutUserUseCase
import com.balex.common.domain.usecases.user.EmitUsersNicknamesListNeedRefreshUseCase
import com.balex.common.extensions.formatStringFirstLetterUppercase
import com.balex.common.extensions.logExceptionToFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AdminRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getRepoAdminUseCase: GetRepoAdminUseCase,
    private val logoutUserUseCase: LogoutUserUseCase,
    private val emitUsersNicknamesListNeedRefreshUseCase: EmitUsersNicknamesListNeedRefreshUseCase,
    private val createFakeUserEmailUseCase: CreateFakeUserEmailUseCase,
    private val context: Context
) : AdminRepository {

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)
    private val deletedTeamCollection = db.collection(FIREBASE_DELETED_TEAM_COLLECTION)
    private val deletedSubUsersCollection = db.collection(FIREBASE_DELETED_SUBUSERS_COLLECTION)

    override suspend fun createNewUser(user: User) {
        val currentUser = getUserUseCase()
        if (currentUser.hasAdminRights) {

            val auth: FirebaseAuth = Firebase.auth
            val admin = getRepoAdminUseCase()

            val newUser = user.copy(
                adminEmailOrPhone = admin.emailOrPhoneNumber.lowercase(),
                hasAdminRights = false,
                hasPremiumAccount = false,
                nickName = user.nickName.formatStringFirstLetterUppercase(),
                displayName = user.displayName.formatStringFirstLetterUppercase(),
                fakeEmail = createFakeUserEmailUseCase(
                    user.nickName.formatStringFirstLetterUppercase(),
                    admin.emailOrPhoneNumber.lowercase()
                ),
                password = user.password,
                availableTasksToAdd = context.resources.getInteger(R.integer.max_available_FCM_per_day_default),
                availableFCM = context.resources.getInteger(R.integer.max_available_FCM_per_day_default),
                teamCoins = context.resources.getInteger(R.integer.available_coins_by_registration)

            )

            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone)
                    .collection(newUser.nickName.lowercase())
                    .document(newUser.nickName.lowercase())

            val adminCollection =
                adminsCollection.document(admin.emailOrPhoneNumber)


            try {
                val adminData: Admin?
                withContext(Dispatchers.IO) {
                    val documentAdminSnapshot = adminCollection.get().await()
                    adminData = documentAdminSnapshot?.toObject(Admin::class.java)
                }
                if (adminData != null) {
                    val newUsersNickNamesList = adminData.usersNickNamesList
                        .toMutableList()
                        .apply { if (!contains(newUser.nickName)) add(newUser.nickName) }
                        .sortedBy { it }
                    val newUserWithId = newUser.copy(id = newUsersNickNamesList.size)
                    withContext(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(
                            newUserWithId.fakeEmail,
                            newUserWithId.password
                        ).await()
                        userCollection.set(newUserWithId).await()
                        adminCollection.update("usersNickNamesList", newUsersNickNamesList).await()
                    }
                    emitUsersNicknamesListNeedRefreshUseCase()
                }
            } catch (e: Exception) {
                logExceptionToFirebase("AdminRepositoryImpl, deleteUser", e.message.toString())
            }
        }
    }


    override suspend fun deleteUser(userName: String) {

        val admin = getRepoAdminUseCase()

        val userCollection =
            usersCollection.document(admin.emailOrPhoneNumber)
                .collection(userName.lowercase())
                .document(userName.lowercase())

        val adminCollection =
            adminsCollection.document(admin.emailOrPhoneNumber)


        try {
            val adminData: Admin?
            withContext(Dispatchers.IO) {
                userCollection.delete().await()
                val documentAdminSnapshot = adminCollection.get().await()
                adminData = documentAdminSnapshot?.toObject(Admin::class.java)
            }

            if (adminData != null) {
                val newUsersNickNamesList = adminData.usersNickNamesList
                    .filter { it != userName }
                    .sortedBy { it }
                    .toMutableList()
                withContext(Dispatchers.IO) {
                    adminCollection.update("usersNickNamesList", newUsersNickNamesList).await()
                }
                emitUsersNicknamesListNeedRefreshUseCase()
            }
        } catch (e: Exception) {
            logExceptionToFirebase("AdminRepositoryImpl, deleteUser", e.message.toString())
        }
        val deletedUser = DeletedSubUser(
            adminEmailOrPhone = admin.emailOrPhoneNumber,
            nickName = userName,
            fakeEmail = createFakeUserEmailUseCase(userName, admin.emailOrPhoneNumber)
        )
        try {
            withContext(Dispatchers.IO) {
                deletedSubUsersCollection.add(deletedUser).await()
            }
        } catch (e: Exception) {
            logExceptionToFirebase("AdminRepositoryImpl, deleteUser", e.message.toString())
        }
    }

    override suspend fun deleteTeam() {
        val currentUser = getUserUseCase()
        val admin = getRepoAdminUseCase()
        if (currentUser.hasAdminRights && currentUser.adminEmailOrPhone == admin.emailOrPhoneNumber) {
            val allUsersCollection =
                usersCollection.document(admin.emailOrPhoneNumber)

            val adminCollection =
                adminsCollection.document(admin.emailOrPhoneNumber)

            try {
                withContext(Dispatchers.IO) {
                    allUsersCollection.delete().await()
                }
            } catch (e: Exception) {
                logExceptionToFirebase("AdminRepositoryImpl, deleteTeam, users", e.message.toString())
            }

            try {
                withContext(Dispatchers.IO) {
                    adminCollection.delete().await()
                }
            } catch (e: Exception) {
                logExceptionToFirebase("AdminRepositoryImpl, deleteTeam, admin", e.message.toString())
            }

            val deletedAdmin = DeletedAdmin(
                emailOrPhoneNumber = admin.emailOrPhoneNumber,
                nickName = admin.nickName,
                fakeEmail = createFakeUserEmailUseCase(admin.nickName, admin.emailOrPhoneNumber),
                usersNickNamesList = admin.usersNickNamesList
            )
            try {
                withContext(Dispatchers.IO) {
                    deletedTeamCollection.add(deletedAdmin).await()
                }
            } catch (e: Exception) {
                logExceptionToFirebase("AdminRepositoryImpl, deleteTeam, deletedTeamCollection.add", e.message.toString())
            }
        }
    }

    override suspend fun deleteSelfAccount(
        userName: String,
        navigateToNotloggedScreen: () -> Unit
    ) {
        deleteUser(userName)
        FirebaseCrashlytics.getInstance().setUserId("")
        logoutUserUseCase()
        navigateToNotloggedScreen()
    }
}


const val FIREBASE_DELETED_TEAM_COLLECTION = "deletedTeam"
const val FIREBASE_DELETED_SUBUSERS_COLLECTION = "deletedSubUsers"