package com.balex.familyteam.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object Storage {

    private const val FILE_NAME = "encrypted_preferences"
    private const val USER_KEY = "shared_prefs_user"
    const val NO_USER_SAVED_IN_SHARED_PREFERENCES = "NO_USER_SAVED_IN_SHARED_PREFERENCES"

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)


    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return (EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ))
    }


    fun getUser(context: Context): String {
        return getEncryptedSharedPreferences(context).getString(
            USER_KEY,
            NO_USER_SAVED_IN_SHARED_PREFERENCES
        ).toString()
    }

    fun saveUser(context: Context, userName: String) {
        getEncryptedSharedPreferences(context).edit().apply {
            putString(USER_KEY, userName)
        }.apply()
    }

    fun saveNoExistUser(context: Context): String {
        return getEncryptedSharedPreferences(context).getString(
            USER_KEY,
            NO_USER_SAVED_IN_SHARED_PREFERENCES
        ).toString()
    }
}