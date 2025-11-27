package app.pandorapass.pandora.logic.services

import app.pandorapass.pandora.logic.models.EncryptionResult
import javax.crypto.SecretKey

/**
 *  Represents a crypto service for encrypting and decrypting data. This is useful for implementing
 *  different crypto services even though we probably wont.
 */
interface CryptoService {

    /**
     * Generates a random salt for the key derivation.
     *
     * @return The generated salt.
     */
    fun generateRandomSalt(): ByteArray

    /**
     * Derives a secure secret key from the user's master password which can then be used for
     * symmetrical encryption.
     *
     * @param password The user's master password.
     * @param salt The salt to use for key derivation.
     * @return The derived key.
     */
    fun deriveKeyAndUnlock(
        password: CharArray,
        salt: ByteArray
    ): SecretKey

    /**
     * Encrypts the given data using the provided secret key.
     *
     * @param data The data to encrypt.
     * @return The encrypted data.
     */
    fun encrypt(
        data: ByteArray,
    ): EncryptionResult

    /**
     * Decrypts the given data using the provided secret key.
     *
     * @param encryptedData The encrypted data.
     * @param iv The initialization vector used for encryption.
     * @return The decrypted data.
     */
    fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray
    ): ByteArray
}

