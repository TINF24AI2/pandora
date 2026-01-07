package app.pandorapass.pandora

import android.app.Application
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import app.pandorapass.pandora.logic.services.BiometricCryptoHelper
import app.pandorapass.pandora.logic.services.CryptoService
import app.pandorapass.pandora.logic.services.impl.CryptoServiceImpl
import app.pandorapass.pandora.data.SettingsDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import app.pandorapass.pandora.logic.models.FileVaultRepository
import app.pandorapass.pandora.logic.services.VaultService
import app.pandorapass.pandora.logic.services.impl.VaultServiceImpl

class PandoraApplication : Application() {
    lateinit var biometricCryptoHelper: BiometricCryptoHelper
    lateinit var biometricTokenStorage: BiometricTokenStorage

    lateinit var cryptoService: CryptoService
    lateinit var vaultService: VaultService
    lateinit var fileVaultRepository: FileVaultRepository

    val settingsDataStore by lazy {
        SettingsDataStore(this)
    }

    private val _lockEvent = MutableSharedFlow<Unit>()
    val lockEvent = _lockEvent.asSharedFlow()

    override fun onCreate() {
        super.onCreate()

        biometricCryptoHelper = BiometricCryptoHelper()
        biometricTokenStorage = BiometricTokenStorage(this)

        cryptoService = CryptoServiceImpl()
        fileVaultRepository = FileVaultRepository(applicationContext)
        vaultService = VaultServiceImpl(cryptoService, fileVaultRepository)
    }

    suspend fun triggerLockEvent() {
        _lockEvent.emit(Unit)
    }
}