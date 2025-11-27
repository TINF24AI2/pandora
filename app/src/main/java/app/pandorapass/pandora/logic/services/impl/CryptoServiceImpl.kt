package app.pandorapass.pandora.logic.services.impl

import app.pandorapass.pandora.logic.models.EncryptionResult
import app.pandorapass.pandora.logic.services.CryptoService
import app.pandorapass.pandora.logic.services.VaultSession
import app.pandorapass.pandora.logic.extensions.toByteArray
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation of the crypto service.
 */
class CryptoServiceImpl : CryptoService {
    private val argon2 = Argon2Kt()
    private val secureRandom = SecureRandom()

    override fun generateRandomSalt(): ByteArray {
        return ByteArray(16).apply { secureRandom.nextBytes(this) }
    }

    override fun deriveKeyAndUnlock(password: CharArray, salt: ByteArray): SecretKeySpec {
        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = salt,
            tCostInIterations = 5,
            mCostInKibibyte = 65536,
            parallelism = 1,
            hashLengthInBytes = 32,
            version = Argon2Version.V13
        )

        val key = SecretKeySpec(result.rawHashAsByteArray().copyOf(32), "AES")

        VaultSession.currentKey = key
        VaultSession.currentSalt = salt

        password.fill('0')

        return key
    }

    override fun encrypt(data: ByteArray): EncryptionResult {
        val key = VaultSession.currentKey ?: throw IllegalStateException("Vault is locked")
        val salt = VaultSession.currentSalt ?: throw IllegalStateException("No salt found")

        val iv = ByteArray(12).apply { secureRandom.nextBytes(this) }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encryptedBytes = cipher.doFinal(data)

        return EncryptionResult(encryptedBytes, salt, iv)
    }

    override fun decrypt(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val key = VaultSession.currentKey ?: throw IllegalStateException("Vault is locked")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(encryptedData)
    }
}