package app.pandorapass.pandora.ui.pages

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.pandorapass.pandora.R
import kotlin.collections.shuffle
import kotlin.math.roundToInt
import java.security.SecureRandom
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.pandorapass.pandora.logic.workers.ClipboardClearWorker
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * Main screen for generating a password.
 *
 * This page allows the user to:
 * - Select which character categories to include (uppercase, lowercase, numbers, special characters)
 * - Adjust the desired password length
 * - Generate a password
 * - Copy the generated password to the clipboard
 *
 * The UI is structured using a Scaffold with a top app bar and a LazyColumn
 * for vertically scrollable grouped settings.
 *
 * @param modifier Optional modifier for external layout control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratePage(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {

    val context = LocalContext.current

    // State for each character category toggle
    var checkedLowercaseLetters by remember { mutableStateOf(true) }
    var checkedUppercaseLetters by remember { mutableStateOf(true) }
    var checkedNumbers by remember { mutableStateOf(true) }
    var checkedSpecial by remember { mutableStateOf(true) }

    // Slider state for password length
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    // Holds the generated password
    var password by remember { mutableStateOf("") }

    val clipboardTimeoutSeconds by settingsViewModel.clipboardTimeout.collectAsState(initial = 60)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Generate") }
            )
        },
        modifier = modifier
    ) { innerPadding ->

        // Scrollable content container
        LazyColumn(contentPadding = innerPadding) {

            // --- Password generation section ---
            item {
                GenerateGroup(title = null) {

                    // Row item that triggers password generation
                    GenerateCopyItem(password, isLastItem = true) {
                        password = generatePassword(
                            checkedUppercaseLetters,
                            checkedLowercaseLetters,
                            checkedNumbers,
                            checkedSpecial,
                            sliderPosition
                        )
                    }
                }

                // Copy button below the group
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            if (password.isBlank()) {
                                Toast.makeText(context, "Generate a password first!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Generated Password", password)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()

                            val timeoutSeconds = clipboardTimeoutSeconds
                            val workManager = WorkManager.getInstance(context)

                            workManager.cancelUniqueWork(ClipboardClearWorker.WORK_NAME)

                            if (timeoutSeconds > 0) {
                                val clearClipboardWorkRequest =
                                    OneTimeWorkRequestBuilder<ClipboardClearWorker>()
                                        .setInitialDelay(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                                        .build()

                                workManager.enqueueUniqueWork(
                                    ClipboardClearWorker.WORK_NAME,
                                    androidx.work.ExistingWorkPolicy.REPLACE,
                                    clearClipboardWorkRequest
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Copy")
                    }
                }
            }

            // --- Length slider section ---
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {

                    // Label showing current length
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Length ")
                            }
                            append(sliderPosition.roundToInt().toString())
                        },
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Slider for selecting password length
                    Slider(
                        value = sliderPosition,
                        onValueChange = { value ->
                            sliderPosition = value.roundToInt().toFloat()
                        },
                        valueRange = 0f..50f,
                        steps = 49
                    )
                }
            }

            // --- Character category toggles ---
            item {
                GenerateGroup(title = "Include") {

                    IncludeItem(
                        title = "Uppercase Letters (A-Z)",
                        checked = checkedUppercaseLetters,
                        onCheckedChange = { checkedUppercaseLetters = it }
                    )

                    IncludeItem(
                        title = "Lowercase Letters (a-z)",
                        checked = checkedLowercaseLetters,
                        onCheckedChange = { checkedLowercaseLetters = it }
                    )

                    IncludeItem(
                        title = "Numbers (0-9)",
                        checked = checkedNumbers,
                        onCheckedChange = { checkedNumbers = it }
                    )

                    IncludeItem(
                        title = "Special Characters (!@#\$%^&*()-_=+[]{};:,.<>?/\\)",
                        checked = checkedSpecial,
                        onCheckedChange = { checkedSpecial = it }
                    )
                }
            }
        }
    }
}


private val secureRandom = SecureRandom()

fun generatePassword(
    uppercaseLetters: Boolean,
    lowercaseLetters: Boolean,
    numbers: Boolean,
    specialCharacters: Boolean,
    length: Float
): String {
    val intLength = length.toInt()
    if (intLength <= 0) return ""

    val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lower = "abcdefghijklmnopqrstuvwxyz"
    val digits = "0123456789"
    val special = "!@#$%^&*()-_=+[]{};:,.<>?/\\"

    val categories = mutableListOf<String>()
    if (uppercaseLetters) categories.add(upper)
    if (lowercaseLetters) categories.add(lower)
    if (numbers) categories.add(digits)
    if (specialCharacters) categories.add(special)

    if (categories.isEmpty()) return ""

    val passwordChars = mutableListOf<Char>()

    // 1. Ensure at least one char from each selected category
    categories.forEach { pool ->
        passwordChars.add(pool[secureRandom.nextInt(pool.length)])
    }

    // 2. Fill the rest
    val allChars = categories.joinToString("")
    // Ensure we don't loop negatively if categories > length
    if (intLength > passwordChars.size) {
        repeat(intLength - passwordChars.size) {
            passwordChars.add(allChars[secureRandom.nextInt(allChars.length)])
        }
    }

    // 3. Shuffle using the secure random source
    passwordChars.shuffle(secureRandom)

    // 4. Handle edge case where selected categories > requested length
    // (Optional: currently it returns the longer password, which is safer)
    return passwordChars.joinToString("")
}
/**
 * A reusable UI section that displays an optional title and a styled container
 * for grouped settings or content.
 *
 * @param title Optional title shown above the group container.
 * @param content Composable content placed inside the group container.
 */
@Composable
fun GenerateGroup(
    title: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    // Outer column providing spacing around the entire group
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        // Draw the title only if it is not null
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp) // Space between title and card
            )
        }

        // Card-like container using Material3 Surface
        Surface(
            shape = MaterialTheme.shapes.medium,              // Rounded corners
            color = MaterialTheme.colorScheme.primaryContainer, // Background color
            modifier = Modifier.fillMaxWidth()                // Full width of parent
        ) {
            // Column that hosts the provided content inside the styled container
            Column {
                content()
            }
        }
    }
}


/**
 * A clickable list item used for actions such as generating or copying a password.
 *
 * Displays an optional title on the left and a trailing icon on the right.
 * Optionally draws a divider below the item unless it is marked as the last item.
 *
 * @param title Optional text shown as the main label of the item.
 * @param isLastItem Whether this item is the final one in the group (controls divider visibility).
 * @param onClick Callback invoked when the item is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateCopyItem(
    title: String?,
    isLastItem: Boolean = false,
    onClick: () -> Unit
) {
    // Main interactive row styled as a Material ListItem
    ListItem(
        modifier = Modifier.clickable { onClick() }, // Entire row is clickable
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent // No background color
        ),
        headlineContent = {
            // Only draw the title if it is not null
            if (title != null) {
                Text(title)
            }
        },
        trailingContent = {
            // Arrow icon shown on the right side of the row
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.arrow_path),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    )

    // Draw a divider below the item unless it's the last one in the group
    if (!isLastItem) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp) // Align with text start
        )
    }
}


/**
 * A settings row that displays a label and a trailing checkbox.
 *
 * The entire row is clickable, allowing users to toggle the checkbox
 * without needing to tap the checkbox directly. A divider is optionally
 * shown below the item unless it is marked as the last item in the group.
 *
 * @param title Text shown as the main label.
 * @param checked Current checked state of the checkbox.
 * @param isLastItem Whether this item is the final one in the group (controls divider visibility).
 * @param onCheckedChange Callback invoked when the checked state should change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncludeItem(
    title: String,
    checked: Boolean,
    isLastItem: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    // Main row styled as a Material ListItem
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) }, // Toggle when row is tapped
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent // No background color
        ),
        headlineContent = {
            Text(title) // Label text
        },
        trailingContent = {
            // Checkbox on the right side
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )

    // Divider below the item unless it's the last one in the group
    if (!isLastItem) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp) // Align with text start
        )
    }
}
