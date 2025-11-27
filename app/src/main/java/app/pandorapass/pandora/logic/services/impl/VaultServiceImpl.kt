package app.pandorapass.pandora.logic.services.impl

import app.pandorapass.pandora.logic.models.FileVaultRepository
import app.pandorapass.pandora.logic.models.VaultEntry
import app.pandorapass.pandora.logic.services.CryptoService
import app.pandorapass.pandora.logic.services.VaultService
import app.pandorapass.pandora.logic.services.VaultSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import kotlinx.serialization.json.Json

/**
 * Implementation of the vault service.
 *
 * @property cryptoService A reference to the crypto service.
 * @property repository A reference to the vault repository.
 */
class VaultServiceImpl(
    private val cryptoService: CryptoService,
    private val repository: FileVaultRepository
) : VaultService {
    override var entries: MutableStateFlow<MutableList<VaultEntry>> = MutableStateFlow(mutableListOf())

    override suspend fun createNewVault(masterPassword: CharArray) {
        val newSalt = cryptoService.generateRandomSalt()

        cryptoService.deriveKeyAndUnlock(masterPassword, newSalt)

        saveVaultToDisk()
    }

    override suspend fun unlock(masterPassword: CharArray) {
        val encryptedFile = repository.load() ?: throw IllegalStateException("No vault found")

        cryptoService.deriveKeyAndUnlock(masterPassword, encryptedFile.salt)

        val jsonBytes = cryptoService.decrypt(encryptedFile.encryptedData, encryptedFile.iv)
        val jsonString = String(jsonBytes, Charsets.UTF_8)

        entries.value = Json.decodeFromString<MutableList<VaultEntry>>(jsonString)
    }

    override fun lock() {
        VaultSession.clear()
        entries.value = mutableListOf()
    }

    override suspend fun addEntry(entry: VaultEntry) {
        entries.value.add(entry)

        saveVaultToDisk()
    }

    override suspend fun updateEntry(entry: VaultEntry) {
        entries.update { currentList ->
            currentList.map { existingEntry ->
                if (existingEntry.id == entry.id) {
                    entry
                } else {
                    existingEntry
                }
            } as MutableList<VaultEntry>
        }

        saveVaultToDisk()
    }

    override suspend fun deleteEntry(id: String) {
        entries.update { currentList ->
            currentList.filter { it.id != id } as MutableList<VaultEntry>
        }

        saveVaultToDisk()
    }

    override suspend fun isVaultInitialized(): Boolean {
        return repository.exists()
    }
    private suspend fun saveVaultToDisk() {
        val jsonString = Json.encodeToString(entries.value)
        val dataBytes = jsonString.toByteArray(Charsets.UTF_8)

        val encryptionResult = cryptoService.encrypt(dataBytes)

        repository.save(encryptionResult)
    }
}
