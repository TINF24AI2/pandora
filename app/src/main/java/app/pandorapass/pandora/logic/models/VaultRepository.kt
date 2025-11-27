package app.pandorapass.pandora.logic.models

/**
 * Represents the vault repository, which is used for loading and storing the vault to a storage place.
 * Using this repository, we can theoretically implement multiple storage locations using the same
 * interface, but realistically, we only want to store the vault locally on the user's device in a file,
 * so this might be a bit overengineered.
 */
interface VaultRepository {
    /**
     * Loads the vault from the storage place.
     *
     * @return The loaded encryption result, if exists.
     */
    suspend fun load(): EncryptionResult?

    /**
     * Saves the vault to the storage place.
     *
     * @param encryptionResult The encrypted vault data to save.
     */
    suspend fun save(encryptionResult: EncryptionResult)

    /**
     * Clears the vault.
     */
    suspend fun clearVault()

    /**
     * Checks whether the vault exists.
     *
     * @return True if the vault exists, false otherwise.
     */
    suspend fun exists(): Boolean
}