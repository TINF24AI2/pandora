package app.pandorapass.pandora.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPage(modifier: Modifier, viewModel: TestVaultViewModel) {
    var query: String by remember { mutableStateOf("") }
    val passwords by viewModel.vaultEntries.collectAsState()
    var addPassword by remember { mutableStateOf(false) }
    var showPasswordEntry by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf("") }

    val filteredPasswords: List<LoginVaultEntry> =
        passwords.filterIsInstance<LoginVaultEntry>().filter { entry ->
            entry.urls?.any { url -> url.contains(query, ignoreCase = true) } == true ||
                    entry.username.contains(query, ignoreCase = true) ||
                    entry.title.contains(query, ignoreCase = true)
        }

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
                            query = it
                        },
                        placeholder = { Text("Search Passwords") },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {}
                    )
                },
                expanded = false,
                onExpandedChange = {}
            ) {} //Lazy Column outside of search bar to not restrict scrolling
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPasswords) { entry ->
                    PasswordItem(
                        entry = entry,
                        showEntry = { showPasswordEntry = true; id = entry.id })
                }
            }
        }
    }
    if (addPassword) {
        AddPassword(viewModel, { addPassword = false })
    }
    if (showPasswordEntry) {
        ShowEntry(viewModel, id, { showPasswordEntry = false })
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
fun ShowEntry(viewModel: TestVaultViewModel, id: String, onDismiss: () -> Unit) {
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
                CopyableTextField(label = "Password", text = loginEntry.password)
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
fun AddPassword(viewModel: TestVaultViewModel, onDismiss: () -> Unit) {
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
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        if (showPassword) R.drawable.eye_slash_24_outlined
                                        else R.drawable.eye_24_outlined
                                    ),
                                    contentDescription = null
                                )
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
                        enabled = (newURL.isNotBlank() && newUsername.isNotBlank() && newPassword.isNotBlank()),
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
            Text(entry.username)
        }
    }
}