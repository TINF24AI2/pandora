package app.pandorapass.pandora.ui.pages

import TimeoutSelectionDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel
import app.pandorapass.pandora.R
import app.pandorapass.pandora.logic.utils.BiometricHelper
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import formatTimeout

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    testVaultViewModel: TestVaultViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val cipherForSetup by settingsViewModel.promptBiometricSetup.collectAsState()
    val errorMsg by settingsViewModel.errorEvent.collectAsState()

    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    val clipboardTimeout by settingsViewModel.clipboardTimeout.collectAsState()
    var showClipboardDialog by remember { mutableStateOf(false) }

    val autoLockTimeout by settingsViewModel.autoLockTimeout.collectAsState()
    var showAutoLockDialog by remember { mutableStateOf(false) }

    if (showClipboardDialog) {
        TimeoutSelectionDialog(
            title = "Clear clipboard after",
            currentTimeout = clipboardTimeout,
            options = listOf(30, 60, 300, -1),
            formatLabel = { seconds ->
                when (seconds) {
                    30 -> "30 Seconds"
                    60 -> "1 Minute"
                    300 -> "5 Minutes"
                    -1 -> "Never"
                    else -> "$seconds Seconds"
                }
            },
            onDismiss = { showClipboardDialog = false },
            onConfirm = { newTimeout ->
                settingsViewModel.onClipboardTimeoutChanged(newTimeout)
                showClipboardDialog = false
            }
        )
    }

    if (showAutoLockDialog) {
        TimeoutSelectionDialog(
            title = "Automatically lock after",
            currentTimeout = autoLockTimeout,
            options = listOf(0, 60, 300, 900, 1800, -1),
            formatLabel = { minutes ->
                when (minutes) {
                    0 -> "Instant"
                    60 -> "1 Minute"
                    300 -> "5 Minutes"
                    900 -> "15 Minutes"
                    1800 -> "30 Minutes"
                    -1 -> "Never"
                    else -> "$minutes Minutes"
                }
            },
            onDismiss = { showAutoLockDialog = false },
            onConfirm = { newTimeout ->
                settingsViewModel.onAutoLockTimeoutChanged(newTimeout)
                showAutoLockDialog = false
            }
        )
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            settingsViewModel.onErrorShown()
        }
    }

    LaunchedEffect(cipherForSetup) {
        if (activity != null && cipherForSetup != null) {
            val executor = ContextCompat.getMainExecutor(context)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    settingsViewModel.onBiometricSetupSucceeded(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    settingsViewModel.onErrorShown()
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
            CenterAlignedTopAppBar(
                title = { Text("Settings") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding
        ) {
            // --- Unlock Group ---
            item {
                SettingsGroup(title = "Unlock") {
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.finger_print_24_filled),
                        title = "Unlock with Biometrics",
                        checked = isBiometricEnabled,
                        isLastItem = true,
                        onCheckedChange = { isChecked ->
                            run {
                                if (BiometricHelper.isBiometricAvailable(context)) {
                                    settingsViewModel.onToggleBiometric(isChecked)
                                } else {
                                    BiometricHelper.promptEnrollBiometric(context)
                                }
                            }
                        }
                    )
                }
            }
            // --- Appearance Group ---
            item {
                SettingsGroup(title = "Appearance") {
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(if (isDarkMode) R.drawable.moon_24_outlined else R.drawable.sun_24_outlined),
                        title = "Theme",
                        subtitle = if (isDarkMode) "Dark Theme" else "Light Theme",
                        checked = isDarkMode,
                        isLastItem = true,
                        onCheckedChange = { isChecked ->
                            settingsViewModel.onThemeChanged(isChecked)
                        }
                    )
                }
            }

            // --- Security Group ---
            item {
                SettingsGroup(title = "Security") {
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.lock_closed_24_outlined),
                        title = "Automatically lock after...",
                        subtitle = formatTimeout(autoLockTimeout), // This one is correct
                        onClick = { showAutoLockDialog = true }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.clipboard_24_outlined),
                        title = "Clear clipboard after",
                        subtitle = formatTimeout(clipboardTimeout),
                        onClick = { showClipboardDialog = true }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.lock_closed_24_outlined),
                        title = "Lock now",
                        withTrailingIcon = false,
                        isLastItem = true,
                        onClick = { testVaultViewModel.lockVault() }
                    )
                }
            }
        }
    }
}

/**
 * A composable that groups settings items together with a background card.
 */
@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * A composable for a standard setting item that navigates somewhere on click.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isLastItem: Boolean = false,
    withTrailingIcon: Boolean = true,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(title) },
        supportingContent = {
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle)
            }
        },
        trailingContent = {
            if (withTrailingIcon) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.chevron_down_24_outlined),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
    if (!isLastItem) {
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
    }
}

/**
 * A composable for a setting item that has a Switch control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    isLastItem: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) }, // Toggle on row click
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(title) },
        supportingContent = {
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle)
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
    if (!isLastItem) {
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
    }
}
