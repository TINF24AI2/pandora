package app.pandorapass.pandora.logic.models

import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Represents an entry in the vault. For now, we just support Login entries, but using this interface
 * we can easily support other types of entries in the future (like credit cards, secure notes, ...)
 */
@Serializable
sealed interface VaultEntry {
    val id: String
    val title: String
    val createdAt: Date
    val updatedAt: Date
}