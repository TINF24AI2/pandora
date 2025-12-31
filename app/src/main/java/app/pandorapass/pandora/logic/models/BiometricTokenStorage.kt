package app.pandorapass.pandora.logic.models

import android.content.Context
import android.util.Base64

/**
 * A data class representing the encrypted biometric data
 *
 * @property iv The initialization vector for the encryption
 * @property encryptedKey The encrypted key
 */
data class EncryptedBiometricData(
    val iv: ByteArray,
    val encryptedKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedBiometricData
        if (!iv.contentEquals(other.iv)) return false
        if (!encryptedKey.contentEquals(other.encryptedKey)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + encryptedKey.contentHashCode()
        return result
    }
}

/**
 * A class representing the biometric token storage. This class is used to store information
 * that is needed to obtain the encryption key using biometric authentication.
 *
 * @param context The application context
 */
class BiometricTokenStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "biometric_secure_prefs"
        private const val KEY_IV = "biometric_iv"
        private const val KEY_ENCRYPTED_BLOB = "biometric_encrypted_blob"
    }

    /**
     * Saves the IV and the encrypted encryption key to SharedPreferences. Uses Base64 to convert
     * the raw ByteArrays into Strings.
     */
    fun storeToken(iv: ByteArray, encryptedKey: ByteArray) {
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val keyString = Base64.encodeToString(encryptedKey, Base64.NO_WRAP)

        prefs.edit().apply {
            putString(KEY_IV, ivString)
            putString(KEY_ENCRYPTED_BLOB, keyString)
            apply()
        }
    }

    /**
     * Retrieves the IV and encrypted encryption key.
     *
     * @return the IV and encrypted encryption key or null if no biometric data is found.
     */
    fun getToken(): EncryptedBiometricData? {
        val ivString = prefs.getString(KEY_IV, null)
        val keyString = prefs.getString(KEY_ENCRYPTED_BLOB, null)

        if (ivString == null || keyString == null) {
            return null
        }

        return try {
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val encryptedKey = Base64.decode(keyString, Base64.NO_WRAP)
            EncryptedBiometricData(iv, encryptedKey)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Clears the stored biometric data.
     */
    fun clearToken() {
        prefs.edit().apply {
            remove(KEY_IV)
            remove(KEY_ENCRYPTED_BLOB)
            apply()
        }
    }

    /**
     * Quick check whether biometric is enabled, for example to update UI.
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.contains(KEY_IV) && prefs.contains(KEY_ENCRYPTED_BLOB)
    }
}