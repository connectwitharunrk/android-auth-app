package com.example.authapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCES_NAME = "auth_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class TokenManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val tokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }

    suspend fun saveUserInfo(userId: String, email: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
        }
    }
}
