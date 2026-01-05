package app.pandorapass.pandora

import android.app.Application
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import app.pandorapass.pandora.logic.services.BiometricCryptoHelper
import app.pandorapass.pandora.logic.services.CryptoService
import app.pandorapass.pandora.logic.services.impl.CryptoServiceImpl
import app.pandorapass.pandora.data.SettingsDataStore

class PandoraApplication : Application() {
    lateinit var biometricCryptoHelper: BiometricCryptoHelper
    lateinit var biometricTokenStorage: BiometricTokenStorage

    lateinit var cryptoService: CryptoService

    val settingsDataStore by lazy {
        SettingsDataStore(this)
    }

    override fun onCreate() {
        super.onCreate()

        biometricCryptoHelper = BiometricCryptoHelper()
        biometricTokenStorage = BiometricTokenStorage(this)

        cryptoService = CryptoServiceImpl()
    }
}