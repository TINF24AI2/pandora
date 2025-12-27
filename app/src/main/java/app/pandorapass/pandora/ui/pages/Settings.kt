package app.pandorapass.pandora.ui.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pandorapass.pandora.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(modifier: Modifier = Modifier) {
    // TODO: get from a ViewModel or DataStore???
    var isDarkMode by remember { mutableStateOf(true) }
    var unlockWithBio by remember { mutableStateOf(true) }
    var unlockWithPin by remember { mutableStateOf(true) }

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
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Unlock with Biometrics",
                        checked = unlockWithBio,
                        onCheckedChange = { unlockWithBio = it }
                    )
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Unlock with PIN",
                        checked = unlockWithPin,
                        isLastItem = true,
                        onCheckedChange = { unlockWithPin = it }
                    )
                }
            }
            // --- Appearance Group ---
            item {
                SettingsGroup(title = "Appearance") {
                    SettingsSwitchItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Theme",
                        subtitle = "Light theme",
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Language",
                        subtitle = "English",
                        isLastItem = true,
                        onClick = { /* TODO: Handle Language click */ }
                    )
                }
            }

            // --- Security Group ---
            item {
                SettingsGroup(title = "Security") {
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Automatically lock after...",
                        subtitle = "15 Minutes",
                        onClick = { /* TODO: Handle Change Password click */ }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Clear clipboard",
                        subtitle = "Never",
                        onClick = { /* TODO: Handle About click */ }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Lock now",
                        withTrailingIcon = false,
                        onClick = { /* TODO: Handle About click */ }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                        title = "Log out",
                        withTrailingIcon = false,
                        isLastItem = true,
                        onClick = { /* TODO: Handle About click */ }
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
    // A Column to hold the title and the settings card
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // The category title (e.g., "Unlock", "Appearance")
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 8.dp) // Space between title and card
        )
        // The card that provides the background and shape
        Surface(
            shape = MaterialTheme.shapes.medium, // Gives it rounded corners
            color = MaterialTheme.colorScheme.primaryContainer, // A subtle background color
            modifier = Modifier.fillMaxWidth()
        ) {
            // A Column to lay out the setting items vertically inside the card
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
                    imageVector = ImageVector.vectorResource(R.drawable.plus_24_outlined),
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

