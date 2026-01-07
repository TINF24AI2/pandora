package app.pandorapass.pandora.ui.activities

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import app.pandorapass.pandora.ui.pages.LoginView
import app.pandorapass.pandora.ui.pages.PandoraApp
import app.pandorapass.pandora.ui.theme.PandoraTheme
import app.pandorapass.pandora.ui.viewmodels.AppState
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModelFactory
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModelFactory
import app.pandorapass.pandora.logic.workers.AutoLockWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class MainActivity : FragmentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            this.application,
            (application as PandoraApplication).settingsDataStore
        )
    }

    private val testVaultViewModel: TestVaultViewModel by viewModels {
        TestVaultViewModelFactory((application as PandoraApplication).vaultService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startPage = if (intent.component?.className == "$packageName.SettingsPage")
            "settings" else "home"

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                (application as PandoraApplication).lockEvent.collect {
                    testVaultViewModel.lockVault()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            PandoraTheme(darkTheme = isDarkMode) {
                val context = LocalContext.current
                val biometricCryptoHelper = (application as PandoraApplication).biometricCryptoHelper

                val viewModel = testVaultViewModel
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
                        onSubmit = { viewModel.createVault(it) }
                    )

                    AppState.LOCKED -> LoginView(
                        onSubmit = { viewModel.unlockVaultWithPassword(it) }
                    )

                    AppState.UNLOCKED -> PandoraApp(startPage, viewModel, settingsViewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        scheduleAutoLock()
    }

    override fun onStart() {
        super.onStart()
        cancelAutoLock()
    }

    private fun scheduleAutoLock() {
        val workManager = WorkManager.getInstance(applicationContext)

        lifecycleScope.launch {
            val currentAppState = testVaultViewModel.appState.value
            if (currentAppState == AppState.LOCKED) {
                return@launch // State is already locked, do nothing.
            }

            val timeoutValue = settingsViewModel.autoLockTimeout.first()
            if (timeoutValue == -1) {
                return@launch // User selected "Never", so do nothing.
            }
            // Stop further execution for the "Instant" case
            if (timeoutValue == 0) {
                testVaultViewModel.lockVault()
                return@launch
            }

            if (timeoutValue > 0) {
                val autoLockWorkRequest = OneTimeWorkRequestBuilder<AutoLockWorker>()
                    .setInitialDelay(timeoutValue.toLong(), TimeUnit.SECONDS)
                    .build()

                workManager.enqueueUniqueWork(
                    AutoLockWorker.WORK_NAME,
                    androidx.work.ExistingWorkPolicy.REPLACE, // Replace any old timer
                    autoLockWorkRequest
                )
            }
        }
    }

    private fun cancelAutoLock() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(AutoLockWorker.WORK_NAME)
    }
}
