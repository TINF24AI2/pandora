package app.pandorapass.pandora.logic.models

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

class FileVaultRepository(context: Context) : VaultRepository {
    private val vaultFile = File(context.filesDir, "pandora_vault.json")

    override suspend fun save(encryptionResult: EncryptionResult) {
        val container = VaultFile(
            saltBase64 = Base64.getEncoder().encodeToString(encryptionResult.salt),
            ivBase64 = Base64.getEncoder().encodeToString(encryptionResult.iv),
            cipherTextBase64 = Base64.getEncoder().encodeToString(encryptionResult.encryptedData)
        )

        val jsonString = Json.encodeToString(container)
        vaultFile.writeText(jsonString)
    }

    override suspend fun load(): EncryptionResult? {
        if (!vaultFile.exists()) return null

        val jsonString = vaultFile.readText()
        val container = Json.decodeFromString<VaultFile>(jsonString)

        return EncryptionResult(
            encryptedData = Base64.getDecoder().decode(container.cipherTextBase64),
            iv = Base64.getDecoder().decode(container.ivBase64),
            salt = Base64.getDecoder().decode(container.saltBase64)
        )
    }

    override suspend fun clearVault() {
        if (vaultFile.exists()) {
            vaultFile.delete()
        }
    }

    override suspend fun exists(): Boolean = vaultFile.exists()
}
