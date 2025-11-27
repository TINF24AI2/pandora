package app.pandorapass.pandora.logic.models

import kotlinx.serialization.Serializable

/**
 * Represents the structure of the actual vault file on the user's device.
 *
 * @property saltBase64 The salt used for key derivation (Argon2id) in base64 format.
 * @property ivBase64 The initialization vector used for encryption (AES-GCM) in base64 format.
 * @property cipherTextBase64 The encrypted data in base64 format.
 */
@Serializable
data class VaultFile(
    val saltBase64: String,
    val ivBase64: String,
    val cipherTextBase64: String
)