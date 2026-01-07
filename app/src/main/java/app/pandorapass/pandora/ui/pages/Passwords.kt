package app.pandorapass.pandora.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.pandorapass.pandora.R
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.ui.viewmodels.VaultViewModel
import java.util.Date
import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.pandorapass.pandora.logic.workers.ClipboardClearWorker
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPage(
    modifier: Modifier,
    viewModel: VaultViewModel,
    settingsViewModel: SettingsViewModel
) {
    val query by viewModel.searchQuery.collectAsState()
    val filteredPasswords by viewModel.filteredPasswords.collectAsState()
    var addPassword by remember { mutableStateOf(false) }
    var showPasswordEntry by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf("") }


    Scaffold(modifier = modifier, floatingActionButton = {
        FloatingActionButton(
            onClick = { addPassword = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.plus_24_outlined),
                "Add login credentials"
            )
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = {
                            viewModel.updateSearchQuery(it)
                        },
                        placeholder = { Text("Search Passwords") },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {}
                    )
                },
                expanded = false,
                onExpandedChange = {}
            ) {}
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredPasswords
                ) { entry ->
                    PasswordItem(
                        entry = entry,
                        showEntry = {
                            showPasswordEntry = true
                            id = entry.id
                        }
                    )
                }
            }
        }
    }
    if (addPassword) {
        AddPassword(viewModel, { addPassword = false })
    }
    if (showPasswordEntry) {
        ShowEntry(viewModel, id, settingsViewModel, { showPasswordEntry = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyableTextField(
    modifier: Modifier = Modifier,
    label: String,
    text: String
) {
    val clipboard: ClipboardManager =
        LocalContext.current.getSystemService(ClipboardManager::class.java)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label)
        OutlinedTextField(
            value = text,
            onValueChange = {},
            modifier = modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(label, text)
                    )
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.square_2_stack_24_outlined),
                        contentDescription = "Copy"
                    )
                }
            },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyablePasswordField(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val clipboardTimeoutSeconds by settingsViewModel.clipboardTimeout.collectAsState(initial = 15)
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label)
        OutlinedTextField(
            value = text,
            onValueChange = {},
            modifier = modifier.fillMaxWidth(),
            readOnly = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row {
                    IconButton(onClick = { visible = !visible }) {
                        if (visible) Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.eye_slash_24_outlined),
                            contentDescription = ""
                        )
                        else Icon(
                            ImageVector.vectorResource(R.drawable.eye_24_outlined),
                            contentDescription = ""
                        )
                    }
                    IconButton(onClick = {
                        val clipboardManager =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(label, text)
                        clipboardManager.setPrimaryClip(clip)
                        // Optionally add a Toast message here

                        // b. Schedule the clipboard to be cleared using WorkManager
                        val timeoutSeconds = clipboardTimeoutSeconds
                        val workManager = WorkManager.getInstance(context)

                        // c. Always cancel any previously scheduled work to reset the timer
                        workManager.cancelUniqueWork(ClipboardClearWorker.WORK_NAME)

                        // d. Only schedule new work if the timeout is not "Never" (-1)
                        if (timeoutSeconds > 0) {
                            val clearClipboardWorkRequest =
                                OneTimeWorkRequestBuilder<ClipboardClearWorker>()
                                    .setInitialDelay(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                                    .build()

                            workManager.enqueueUniqueWork(
                                ClipboardClearWorker.WORK_NAME,
                                androidx.work.ExistingWorkPolicy.REPLACE, // Replace old timer
                                clearClipboardWorkRequest
                            )
                        }
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                R.drawable.square_2_stack_24_outlined
                            ),
                            contentDescription = "Copy"
                        )
                    }
                }
            },
            singleLine = true
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowEntry(
    viewModel: VaultViewModel,
    id: String,
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val entries by viewModel.vaultEntries.collectAsState()
    val loginEntry =
        entries.filterIsInstance<LoginVaultEntry>().find { it.id == id } ?: LoginVaultEntry(
            id, "Something went wrong...", "", "", "", null, Date(), Date()
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(loginEntry.title)
                CopyableTextField(label = "Username", text = loginEntry.username)
                CopyablePasswordField(
                    label = "Password",
                    text = loginEntry.password,
                    settingsViewModel = settingsViewModel // Pass it here
                )
                loginEntry.urls?.forEach { url ->
                    CopyableTextField(label = "URL", text = url)
                }
                CopyableTextField(label = "Notes", text = loginEntry.notes ?: "")
                Button(onClick = { viewModel.deleteEntry(id); onDismiss() }) { Text("Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassword(viewModel: VaultViewModel, onDismiss: () -> Unit) {
    var showPassword by remember { mutableStateOf(false) }

    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newURL by remember { mutableStateOf("") }
    var urls by remember { mutableStateOf(listOf<String>()) }
    var newTitle by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val width = Modifier.fillMaxWidth()
                item { Text("Create new Password") }
                item {
                    OutlinedTextField(
                        modifier = width,
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = width,
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Username") },
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = width,
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password") },
                        visualTransformation =
                            if (!showPassword) PasswordVisualTransformation()
                            else VisualTransformation.None,
                        trailingIcon = {
                            Row(
                                modifier = Modifier.padding(end = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    newPassword = generatePassword(true, true, true, true, 16.0F)
                                }) {
                                    Icon(
                                        ImageVector.vectorResource(R.drawable.arrow_path),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(
                                            if (showPassword) R.drawable.eye_slash_24_outlined
                                            else R.drawable.eye_24_outlined
                                        ),
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                }
                items(urls) { url ->
                    Text(url)
                }
                item {
                    OutlinedTextField(
                        modifier = width,
                        value = newURL,
                        onValueChange = { newURL = it },
                        label = { Text("URL") },
                        trailingIcon = {
                            IconButton(onClick = { urls = urls + newURL; newURL = "" }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.plus_24_outlined),
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = width,
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notes") },
                        singleLine = true
                    )
                }
                item {
                    Button(
                        modifier = width,
                        enabled = (newUsername.isNotBlank() && newPassword.isNotBlank()),
                        onClick = {
                            viewModel.addLoginEntry(
                                newTitle,
                                newUsername,
                                newPassword,
                                newNotes,
                                (urls + newURL).filter { it.isNotBlank() }.distinct()
                            )
                            onDismiss()
                        }) {
                        Text("Add Password")
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordItem(modifier: Modifier = Modifier, entry: LoginVaultEntry, showEntry: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { showEntry() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(entry.title)
            //Text(entry.username)
        }
    }
}