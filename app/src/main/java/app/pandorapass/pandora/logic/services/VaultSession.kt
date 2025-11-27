package app.pandorapass.pandora.logic.services

import javax.crypto.SecretKey

/**
 * A singleton representing the information of the current in-memory session (storing the encryption
 * key and salt).
 */
object VaultSession {
    var currentKey: SecretKey? = null
    var currentSalt: ByteArray? = null

    /**
     * Checks whether the vault is currently unlocked.
     *
     * @return Whether the vault is unlocked or not.
     */
    fun isVaultUnlocked(): Boolean = currentKey != null

    /**
     * Clears the current session
     */
    fun clear() {
        currentKey = null
        currentSalt = null
    }
}