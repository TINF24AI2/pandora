// In app/pandorapass/pandora/ui/pages/SettingsDialogs.kt
package app.pandorapass.pandora.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Helper to format seconds into a user-friendly string
fun formatTimeout(seconds: Int): String {
    return when (seconds) {
        15 -> "15 Seconds"
        30 -> "30 Seconds"
        60 -> "1 Minute"
        -1 -> "Never" // -1 will represent the "Never clear" option
        else -> "$seconds Seconds"
    }
}

@Composable
fun ClipboardTimeoutDialog(
    currentTimeout: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val options = listOf(15, 30, 60, -1)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(currentTimeout) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear clipboard after") },
        text = {
            Column {
                options.forEach { timeout ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (timeout == selectedOption),
                                onClick = { onOptionSelected(timeout) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (timeout == selectedOption),
                            onClick = { onOptionSelected(timeout) }
                        )
                        Text(
                            text = formatTimeout(timeout),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedOption) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
