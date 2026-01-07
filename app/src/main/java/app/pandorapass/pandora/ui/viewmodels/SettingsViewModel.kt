package app.pandorapass.pandora.ui.viewmodels

import android.app.Application
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.services.VaultSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import app.pandorapass.pandora.data.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    application: Application,
    private val settingsDataStore: SettingsDataStore,
) : AndroidViewModel(application) {
    private val tokenStorage = (application as PandoraApplication).biometricTokenStorage
    private val cryptoHelper = (application as PandoraApplication).biometricCryptoHelper

    val isDarkMode = settingsDataStore.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val clipboardTimeout = settingsDataStore.clipboardTimeout
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 30
        )

    val autoLockTimeout = settingsDataStore.autoLockTimeout
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 300
        )

    private val _isBiometricEnabled = MutableStateFlow(tokenStorage.isBiometricEnabled())
    val isBiometricEnabled = _isBiometricEnabled.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    private val _promptBiometricSetup = MutableStateFlow<Cipher?>(null)
    val promptBiometricSetup = _promptBiometricSetup.asStateFlow()

    fun onToggleBiometric(enabled: Boolean) {
        if (!enabled) {
            tokenStorage.clearToken()
            _isBiometricEnabled.value = false
            return
        }

        val currentKey = VaultSession.currentKey
        if (currentKey == null) {
            _errorEvent.value = "Session expired. Please re-login."
            return
        }

        try {
            val cipher = cryptoHelper.getCipherForEncryption()

            _promptBiometricSetup.value = cipher
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.toString())
            if (e is KeyPermanentlyInvalidatedException) {
                tokenStorage.clearToken()
                _isBiometricEnabled.value = false
                cryptoHelper.clearKey()
                onToggleBiometric(true)
                return
            }
            _errorEvent.value = e.message
        }
    }

    fun onBiometricSetupSucceeded(result: BiometricPrompt.AuthenticationResult) {
        viewModelScope.launch(Dispatchers.IO) {
            val cipher = result.cryptoObject?.cipher ?: return@launch
            val liveMasterKey = VaultSession.currentKey ?: return@launch

            val encryptedBytes = cipher.doFinal(liveMasterKey.encoded)
            val iv = cipher.iv

            tokenStorage.storeToken(iv, encryptedBytes)
            _isBiometricEnabled.value = true

            _promptBiometricSetup.value = null
        }
    }

    fun onThemeChanged(isDarkMode: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(isDarkMode)
        }
    }

    fun onClipboardTimeoutChanged(timeoutInSeconds: Int) {
        viewModelScope.launch {
            settingsDataStore.setClipboardTimeout(timeoutInSeconds)
        }
    }

    fun onAutoLockTimeoutChanged(timeoutInSeconds: Int) {
        viewModelScope.launch {
            settingsDataStore.setAutoLockTimeout(timeoutInSeconds)
        }
    }

    fun onErrorShown() {
        _errorEvent.value = null
    }
}