package app.pandorapass.pandora.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import app.pandorapass.pandora.ui.pages.LoginView
import app.pandorapass.pandora.ui.pages.PandoraApp
import app.pandorapass.pandora.ui.theme.PandoraTheme
import app.pandorapass.pandora.ui.viewmodels.AppState
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModelFactory
import app.pandorapass.pandora.ui.viewmodels.VaultViewModel
import app.pandorapass.pandora.ui.viewmodels.VaultViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            this.application,
            (application as PandoraApplication).settingsDataStore
        )
    }

    private val vaultViewModel: VaultViewModel by viewModels {
        VaultViewModelFactory((application as PandoraApplication).vaultService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                (application as PandoraApplication).lockEvent.collect {
                    vaultViewModel.lockVault()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            PandoraTheme(darkTheme = isDarkMode) {
                val context = LocalContext.current
                val biometricCryptoHelper =
                    (application as PandoraApplication).biometricCryptoHelper

                val viewModel = vaultViewModel
                val appState by viewModel.appState.collectAsState()
                val error by viewModel.error.collectAsState()

                val biometricStorage = BiometricTokenStorage(context)
                if (biometricStorage.isBiometricEnabled() && appState == AppState.LOCKED) {
                    biometricCryptoHelper.showBiometricUnlock(
                        activity = this,
                        application = application as PandoraApplication,
                        onSuccess = { decryptedMasterKey ->
                            run {
                                viewModel.unlockVaultWithKey(decryptedMasterKey)
                            }
                        }
                    )
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(8.dp)
                            .zIndex(1f)
                            .statusBarsPadding()
                    )
                }

                when (appState) {
                    AppState.LOADING -> CircularProgressIndicator()

                    AppState.SETUP -> LoginView(
                        firstTimeLogin = true,
                        onSubmit = { viewModel.createVault(it) }
                    )

                    AppState.LOCKED -> LoginView(
                        firstTimeLogin = false,
                        onSubmit = { viewModel.unlockVaultWithPassword(it) }
                    )

                    AppState.UNLOCKED -> PandoraApp(viewModel, settingsViewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
    }
}