package app.pandorapass.pandora.ui.pages.dialogs

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
        0 -> "Instant"
        30 -> "30 Seconds"
        60 -> "1 Minute"
        300 -> "5 Minutes"
        900 -> "15 Minutes"
        1800 -> "30 Minutes"
        -1 -> "Never" // -1 will represent the "Never" option
        else -> "$seconds Seconds"
    }
}

@Composable
fun TimeoutSelectionDialog(
    title: String,
    currentTimeout: Int,
    options: List<Int>,
    formatLabel: (Int) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(currentTimeout) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                            text = formatLabel(timeout),
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
