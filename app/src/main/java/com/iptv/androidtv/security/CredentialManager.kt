package com.iptv.androidtv.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.iptv.androidtv.data.IPTVCredentials

/**
 * Manages secure storage and retrieval of IPTV credentials using Android's encrypted preferences
 */
class CredentialManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "iptv_credentials"
        private const val KEY_HOST = "host"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_HAS_CREDENTIALS = "has_credentials"
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Saves IPTV credentials securely to encrypted storage
     * @param credentials The credentials to save
     * @return Result indicating success or failure with error message
     */
    fun saveCredentials(credentials: IPTVCredentials): Result<Unit> {
        return try {
            if (!credentials.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid credentials provided"))
            }

            with(encryptedPrefs.edit()) {
                putString(KEY_HOST, credentials.host)
                putString(KEY_USERNAME, credentials.username)
                putString(KEY_PASSWORD, credentials.password)
                putBoolean(KEY_HAS_CREDENTIALS, true)
                apply()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to save credentials: ${e.message}", e))
        }
    }

    /**
     * Retrieves stored IPTV credentials
     * @return IPTVCredentials if found and valid, null otherwise
     */
    fun getCredentials(): IPTVCredentials? {
        return try {
            if (!hasCredentials()) {
                return null
            }

            val host = encryptedPrefs.getString(KEY_HOST, null)
            val username = encryptedPrefs.getString(KEY_USERNAME, null)
            val password = encryptedPrefs.getString(KEY_PASSWORD, null)

            if (host != null && username != null && password != null) {
                val credentials = IPTVCredentials(host, username, password)
                if (credentials.isValid()) {
                    credentials
                } else {
                    // Clear invalid credentials
                    clearCredentials()
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            // If decryption fails, clear potentially corrupted data
            clearCredentials()
            null
        }
    }

    /**
     * Checks if credentials are stored
     * @return true if credentials exist, false otherwise
     */
    fun hasCredentials(): Boolean {
        return try {
            encryptedPrefs.getBoolean(KEY_HAS_CREDENTIALS, false)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clears all stored credentials
     * @return Result indicating success or failure
     */
    fun clearCredentials(): Result<Unit> {
        return try {
            with(encryptedPrefs.edit()) {
                remove(KEY_HOST)
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD)
                remove(KEY_HAS_CREDENTIALS)
                apply()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to clear credentials: ${e.message}", e))
        }
    }

    /**
     * Updates existing credentials with new values
     * @param credentials The new credentials to save
     * @return Result indicating success or failure
     */
    fun updateCredentials(credentials: IPTVCredentials): Result<Unit> {
        // Clear existing credentials first, then save new ones
        return clearCredentials().fold(
            onSuccess = { saveCredentials(credentials) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Validates stored credentials without retrieving them
     * @return true if stored credentials are valid, false otherwise
     */
    fun areStoredCredentialsValid(): Boolean {
        return getCredentials()?.isValid() == true
    }
}