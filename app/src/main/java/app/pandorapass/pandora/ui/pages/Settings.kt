import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val cipherForSetup by viewModel.promptBiometricSetup.collectAsState()
    val errorMsg by viewModel.errorEvent.collectAsState()

    LaunchedEffect(errorMsg) {
        errorMsg?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
    }

    LaunchedEffect(cipherForSetup) {
        if (activity != null && cipherForSetup != null) {
            val executor = ContextCompat.getMainExecutor(context)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSetupSucceeded(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.onErrorShown()
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable Biometric Unlock")
                .setSubtitle("Scan your fingerprint to confirm")
                .setNegativeButtonText("Cancel")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            try {
                biometricPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(cipherForSetup!!)
                )
            } catch (e: Exception) {
                // TODO: should probably add some proper error handling here later. If this is still
                // in the final commit, this is surely intended :)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Security Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Biometric Unlock",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Use fingerprint or face ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { isChecked ->
                        viewModel.onToggleBiometric(isChecked)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}