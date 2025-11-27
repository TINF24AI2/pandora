package app.pandorapass.pandora.logic.services

import app.pandorapass.pandora.logic.models.VaultEntry
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Represents the API of the password vault service.
 */
interface VaultService {
    val entries: MutableStateFlow<MutableList<VaultEntry>>

    /**
     * Checks whether the vault is already initialized (exists) or whether we should start the setup
     *
     * @return Whether the vault is initialized or not.
     */
    suspend fun isVaultInitialized(): Boolean

    /**
     * Creates a new vault with the given master password.
     *
     * @param masterPassword The user's master password.
     */
    suspend fun createNewVault(masterPassword: CharArray)

    /**
     * Tries to open an existing vault with the given master password.
     *
     * @param masterPassword The user's master password.
     */
    suspend fun unlock(masterPassword: CharArray)

    /**
     * Locks the currently open vault.
     */
    fun lock()

    /**
     * Adds a new entry to the vault.
     *
     * @param entry The vault entry to add.
     */
    suspend fun addEntry(entry: VaultEntry)

    /**
     * Updates an existing entry in the vault.
     *
     * @param entry The entry to update.
     */
    suspend fun updateEntry(entry: VaultEntry)

    /**
     * Deletes an existing entry from the vault.
     *
     * @param id The ID of the entry to delete.
     */
    suspend fun deleteEntry(id: String)
}