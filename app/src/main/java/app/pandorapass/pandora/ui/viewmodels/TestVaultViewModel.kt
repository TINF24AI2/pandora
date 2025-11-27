package app.pandorapass.pandora.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.logic.models.VaultEntry
import app.pandorapass.pandora.logic.services.VaultService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

enum class AppState {
    LOADING, SETUP, LOCKED, UNLOCKED
}

class TestVaultViewModel(
    private val vaultService: VaultService
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState.LOADING)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val vaultEntries: StateFlow<List<VaultEntry>> = vaultService.entries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        checkVaultStatus()
    }

    private fun checkVaultStatus() {
        viewModelScope.launch {
            if (vaultService.isVaultInitialized()) {
                _appState.value = AppState.LOCKED
            } else {
                _appState.value = AppState.SETUP
            }
        }
    }

    fun createVault(password: String) {
        viewModelScope.launch {
            try {
                vaultService.createNewVault(password.toCharArray())
                _appState.value = AppState.UNLOCKED
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Setup failed: ${e.message}"
            }
        }
    }

    fun unlockVault(password: String) {
        viewModelScope.launch {
            try {
                vaultService.unlock(password.toCharArray())
                _appState.value = AppState.UNLOCKED
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Wrong password or decryption failed."
            }
        }
    }

    fun lockVault() {
        vaultService.lock()
        _appState.value = AppState.LOCKED
    }

    fun addSampleEntry(title: String, username: String, secret: String) {
        viewModelScope.launch {
            val newEntry = LoginVaultEntry(
                id = UUID.randomUUID().toString(),
                title = title,
                username = username,
                password = secret,
                notes = "",
                urls = listOf("https://example.com"),
                createdAt = Date(),
                updatedAt = Date()
            )
            vaultService.addEntry(newEntry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            vaultService.deleteEntry(id)
        }
    }
}