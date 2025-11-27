package app.pandorapass.pandora.logic.models

/**
 * Represents an encrypted data.
 *
 * @property encryptedData The encrypted data.
 * @property salt The salt used for key derivation (Argon2id).
 * @property iv The initialization vector used for encryption (AES-GCM).
 */
data class EncryptionResult(
    val encryptedData: ByteArray,
    val salt: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionResult

        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedData.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
