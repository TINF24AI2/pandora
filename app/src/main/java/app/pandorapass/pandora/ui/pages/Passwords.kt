package app.pandorapass.pandora.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
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
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()


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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                query = query,
                onQueryChange = {
                    viewModel.updateSearchQuery(it)
                },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search your passwords") },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            R.drawable.search_24_outline
                        ),
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            ) {}
            Text(
                text = "Passwords",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        items = filteredPasswords
                    ) { entry ->
                        PasswordItem(
                            entry = entry,
                            showEntry = {
                                showPasswordEntry = true
                                id = entry.id
                            },
                            isDarkMode = isDarkMode
                        )
                        if (filteredPasswords.last() != entry) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
    if (addPassword) {
        AddPassword(viewModel) { addPassword = false }
    }
    if (showPasswordEntry) {
        ShowEntry(viewModel, id, settingsViewModel) { showPasswordEntry = false }
    }
}

@Composable
private fun InfoListItem(label: String, value: String, trailingContent: @Composable () -> Unit) {
    ListItem(
        headlineContent = { Text(value) },
        overlineContent = { Text(label) },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun PasswordInfoListItem(settingsViewModel: SettingsViewModel, password: String) {
    val context = LocalContext.current
    val clipboardTimeoutSeconds by settingsViewModel.clipboardTimeout.collectAsState(initial = 15)
    var visible by rememberSaveable { mutableStateOf(false) }
    val visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()

    ListItem(
        headlineContent = {
            Text(
                text = visualTransformation.filter(AnnotatedString(password)).text.toString(),
            )
        },
        overlineContent = { Text("Password") },
        trailingContent = {
            Row {
                IconButton(onClick = { visible = !visible }) {
                    if (visible) Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.eye_slash_24_outlined),
                        contentDescription = "Hide password"
                    )
                    else Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.eye_24_outlined),
                        contentDescription = "Show password"
                    )
                }
                IconButton(onClick = {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Password", password)
                    clipboardManager.setPrimaryClip(clip)

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
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
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
    val context = LocalContext.current
    val clipboard: ClipboardManager = context.getSystemService(ClipboardManager::class.java)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("View Login", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ListItem(
                    headlineContent = { Text(loginEntry.title) },
                    leadingContent = {
                        Avatar(
                            modifier = Modifier.size(40.dp),
                            text = if (loginEntry.title.isNotEmpty()) loginEntry.title.first().toString() else ""
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Text("Credentials", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column {
                    InfoListItem(
                        label = "Username",
                        value = loginEntry.username,
                        trailingContent = {
                            IconButton(onClick = {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Username", loginEntry.username))
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.square_2_stack_24_outlined),
                                    contentDescription = "Copy"
                                )
                            }
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    PasswordInfoListItem(settingsViewModel = settingsViewModel, password = loginEntry.password)

                }
            }

            if (loginEntry.urls?.isNotEmpty() == true || loginEntry.notes?.isNotBlank() == true) {
                Text("Other Information", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column {
                        loginEntry.urls?.forEach { url ->
                            InfoListItem(
                                label = "Website URL",
                                value = url,
                                trailingContent = {
                                    IconButton(onClick = {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", url))
                                    }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.square_2_stack_24_outlined),
                                            contentDescription = "Copy"
                                        )
                                    }
                                }
                            )
                            if (loginEntry.urls.last() != url || loginEntry.notes?.isNotBlank() == true) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                        if (loginEntry.notes?.isNotBlank() == true) {
                            InfoListItem(
                                label = "Notes",
                                value = loginEntry.notes!!,
                                trailingContent = {
                                    IconButton(onClick = {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Notes", loginEntry.notes))
                                    }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.square_2_stack_24_outlined),
                                            contentDescription = "Copy"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Button(onClick = { viewModel.deleteEntry(id); onDismiss() }) { Text("Delete") }
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
fun Avatar(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun PasswordItem(
    modifier: Modifier = Modifier,
    entry: LoginVaultEntry,
    showEntry: () -> Unit,
    isDarkMode: Boolean = false
) {
    ListItem(
        modifier = modifier.clickable { showEntry() },
        headlineContent = { Text(entry.title) },
        supportingContent = { Text(entry.username) },
        leadingContent = {
            Avatar(
                modifier = Modifier.size(40.dp),
                text = if (entry.title.isNotEmpty()) entry.title.first().toString() else ""
            )
        },
        trailingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ellipsis_horizontal_20_solid),
                contentDescription = "Show details"
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}
