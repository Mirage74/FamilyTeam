package com.balex.common.data.repository

import android.content.Context
import android.util.Log
import com.balex.common.R
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_ADMINS_COLLECTION
import com.balex.common.data.repository.RegLogRepositoryImpl.Companion.FIREBASE_USERS_COLLECTION
import com.balex.common.domain.entity.Admin
import com.balex.common.domain.entity.DeletedSubUser
import com.balex.common.domain.entity.User
import com.balex.common.domain.repository.AdminRepository
import com.balex.common.domain.usecases.regLog.CreateFakeUserEmailUseCase
import com.balex.common.domain.usecases.regLog.GetRepoAdminUseCase
import com.balex.common.domain.usecases.regLog.GetUserUseCase
import com.balex.common.domain.usecases.user.EmitUsersNicknamesListNeedRefreshUseCase
import com.balex.common.extensions.formatStringFirstLetterUppercase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class AdminRepositoryImpl @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getRepoAdminUseCase: GetRepoAdminUseCase,
    private val emitUsersNicknamesListNeedRefreshUseCase: EmitUsersNicknamesListNeedRefreshUseCase,
    private val createFakeUserEmailUseCase: CreateFakeUserEmailUseCase,
    private val context: Context
) : AdminRepository {

    private val db = Firebase.firestore
    private val adminsCollection = db.collection(FIREBASE_ADMINS_COLLECTION)
    private val usersCollection = db.collection(FIREBASE_USERS_COLLECTION)
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
                availableFCM = context.resources.getInteger(R.integer.max_available_FCM_per_day_default)
            )

            val userCollection =
                usersCollection.document(newUser.adminEmailOrPhone)
                    .collection(newUser.nickName.lowercase())
                    .document(newUser.nickName.lowercase())

            val adminCollection =
                adminsCollection.document(admin.emailOrPhoneNumber)


            try {
                auth.createUserWithEmailAndPassword(newUser.fakeEmail, newUser.password).await()
                userCollection.set(newUser).await()

                val documentAdminSnapshot = adminCollection.get().await()
                val adminData = documentAdminSnapshot?.toObject(Admin::class.java)
                if (adminData != null) {
                    val newUsersNickNamesList = adminData.usersNickNamesList
                        .toMutableList()
                        .apply { if (!contains(newUser.nickName)) add(newUser.nickName) }
                        .sortedBy { it }

                    adminCollection.update("usersNickNamesList", newUsersNickNamesList).await()
                    emitUsersNicknamesListNeedRefreshUseCase()
                }
            } catch (e: Exception) {
                Log.d("AdminRepositoryImpl", "deleteUser, Error: ${e.message}")
            }
        }
    }

    override suspend fun deleteUser(userName: String) {
        val currentUser = getUserUseCase()
        if (currentUser.hasAdminRights) {

            val admin = getRepoAdminUseCase()

            val userCollection =
                usersCollection.document(admin.emailOrPhoneNumber)
                    .collection(userName.lowercase())
                    .document(userName.lowercase())

            val adminCollection =
                adminsCollection.document(admin.emailOrPhoneNumber)


            try {
                userCollection.delete().await()

                val documentAdminSnapshot = adminCollection.get().await()
                val adminData = documentAdminSnapshot?.toObject(Admin::class.java)
                if (adminData != null) {
                    val newUsersNickNamesList = adminData.usersNickNamesList
                        .filter { it != userName }
                        .sortedBy { it }
                        .toMutableList()
                    adminCollection.update("usersNickNamesList", newUsersNickNamesList).await()
                    emitUsersNicknamesListNeedRefreshUseCase()
                }
            } catch (e: Exception) {
                Log.d("AdminRepositoryImpl", "deleteUser del data, Error: ${e.message}")
            }
            val deletedUser = DeletedSubUser(
                adminEmailOrPhone = admin.emailOrPhoneNumber,
                nickName = userName,
                fakeEmail = createFakeUserEmailUseCase(userName, admin.emailOrPhoneNumber)
            )
            try {
                deletedSubUsersCollection.add(deletedUser).await()
            } catch (e: Exception) {
                Log.d("AdminRepositoryImpl", "deleteUser add to collection, Error: ${e.message}")
            }

        }
    }
}

const val FIREBASE_DELETED_SUBUSERS_COLLECTION = "deletedSubUsers"