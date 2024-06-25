package com.balex.familyteam.data.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object Storage {

    private const val FILE_NAME = "encrypted_preferences"
    private const val USER_KEY = "shared_prefs_user"
    private const val LANGUAGE_KEY = "shared_prefs_language"
    const val NO_USER_SAVED_IN_SHARED_PREFERENCES = "NO_USER_SAVED_IN_SHARED_PREFERENCES"
    const val NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES = "NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES"

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
        ).toString().trim()
    }

    fun saveUser(context: Context, userName: String) {
        getEncryptedSharedPreferences(context).edit().apply {
            putString(USER_KEY, userName.trim())
        }.apply()
    }

    fun getLanguage(context: Context): String {
        return getEncryptedSharedPreferences(context).getString(
            LANGUAGE_KEY,
            NO_LANGUAGE_SAVED_IN_SHARED_PREFERENCES
        ).toString().trim()
    }

    fun saveLanguage(context: Context, language: String) {
        getEncryptedSharedPreferences(context).edit().apply {
            putString(LANGUAGE_KEY, language.trim())
        }.apply()
    }
}