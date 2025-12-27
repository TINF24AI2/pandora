package app.pandorapass.pandora.logic.services

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class BiometricCryptoHelper {

    companion object {
        private const val KEY_NAME = "my_biometric_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * Creates the key in the hardware keystore if it doesn't exist. This is needed for the biometric
     * authentication.
     */
    fun generateBiometricKey() {
        if (keyStore.containsAlias(KEY_NAME)) return

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Takes the raw encryption key bytes and returns a pair (encryptionKeyBytes, IV).
     * Note: We usually don't need user auth to ENCRYPT, only to DECRYPT.
     */
    fun encryptMasterKey(masterKeyBytes: ByteArray): Pair<ByteArray, ByteArray> {
        generateBiometricKey()

        val secretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(masterKeyBytes)
        return Pair(encryptedBytes, cipher.iv)
    }

    /**
     * Returns a cipher that is ready to be passed to BiometricPrompt.
     */
    fun getCipherForDecryption(iv: ByteArray): Cipher {
        val secretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher
    }

    fun getCipherForEncryption(): Cipher {
        generateBiometricKey()

        val secretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return cipher
    }

    /**
     * Displays the biometric authentication sheet.
     *
     * @param activity The activity that requested the biometric authentication
     * @param application A reference to the pandora application
     * @param onSuccess A callback on what should happen when the authentication is successful
     */
    fun showBiometricUnlock(
        activity: FragmentActivity,
        application: PandoraApplication,
        onSuccess: (ByteArray) -> Unit
    ) {
        val token = application.biometricTokenStorage.getToken()
        if (token == null) {
            Toast.makeText(activity, "Biometric login not set up.", Toast.LENGTH_SHORT).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val cipher = result.cryptoObject?.cipher
                if (cipher != null) {
                    try {
                        val decryptedMasterKey = cipher.doFinal(token.encryptedKey)

                        onSuccess(decryptedMasterKey)
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Decryption failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(activity, "Auth Error: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        }

        try {
            val cipher = application.biometricCryptoHelper.getCipherForDecryption(token.iv)

            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Vault")
                .setSubtitle("Use your fingerprint to login")
                .setNegativeButtonText("Use Password instead")
                .build()

            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))

        } catch (e: Exception) {
            Toast.makeText(activity, "Biometric key invalidated. Please log in with password.", Toast.LENGTH_LONG).show()
            application.biometricTokenStorage.clearToken()
        }
    }
}