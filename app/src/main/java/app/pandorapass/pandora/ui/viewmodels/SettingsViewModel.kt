package app.pandorapass.pandora.ui.viewmodels

import android.app.Application
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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = (application as PandoraApplication).biometricTokenStorage
    private val cryptoHelper = (application as PandoraApplication).biometricCryptoHelper

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
            // TODO: Handle error
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

    fun onErrorShown() {
        _errorEvent.value = null
    }
}