package app.pandorapass.pandora.logic.models

import app.pandorapass.pandora.logic.utils.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * A login (username, password, url, notes) vault entry.
 *
 * @property id The id of the entry.
 * @property title The title of the login.
 * @property username The username of the login.
 * @property password The password of the login.
 * @property url (Optional) The url of the login.
 * @property notes (Optional) Additional notes for the login.
 * @property createdAt The creation date of the entry.
 * @property updatedAt The latest update date of the entry.
 */
@Serializable
@SerialName("login")
data class LoginVaultEntry(
    override val id: String,
    override val title: String,
    val username: String,
    val password: String,
    val notes: String?,
    val urls: List<String>?,

    @Serializable(with = DateSerializer::class)
    override val createdAt: Date,

    @Serializable(with = DateSerializer::class)
    override val updatedAt: Date
) : VaultEntry