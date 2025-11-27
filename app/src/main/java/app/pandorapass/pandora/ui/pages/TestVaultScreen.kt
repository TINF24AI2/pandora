package app.pandorapass.pandora.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.logic.models.VaultEntry
import app.pandorapass.pandora.ui.viewmodels.AppState
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel

@Composable
fun TestVaultScreen(viewModel: TestVaultViewModel) {
    val appState by viewModel.appState.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red)
                        .padding(8.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                )
            }

            when (appState) {
                AppState.LOADING -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                AppState.SETUP -> AuthScreen(
                    title = "Create New Vault",
                    btnLabel = "Initialize",
                    onSubmit = { viewModel.createVault(it) }
                )

                AppState.LOCKED -> AuthScreen(
                    title = "Vault Locked",
                    btnLabel = "Unlock",
                    onSubmit = { viewModel.unlockVault(it) }
                )

                AppState.UNLOCKED -> VaultContentsScreen(viewModel)
            }
        }
    }
}

@Composable
fun AuthScreen(title: String, btnLabel: String, onSubmit: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔒", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Master Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "HIDE" else "SHOW")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSubmit(password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = password.isNotEmpty()
        ) {
            Text(btnLabel)
        }
    }
}

@Composable
fun VaultContentsScreen(viewModel: TestVaultViewModel) {
    val entries by viewModel.vaultEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", fontSize = 24.sp)
            }
        },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Vault",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { viewModel.lockVault() }) {
                    Text("LOCK")
                }
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Vault is empty. Add an entry!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(entries) { entry ->
                    VaultEntryItem(entry, onDelete = { viewModel.deleteEntry(entry.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, user, pass ->
                viewModel.addSampleEntry(title, user, pass)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun VaultEntryItem(entry: VaultEntry, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = entry.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (entry is LoginVaultEntry) {
                    Text(text = entry.username, color = Color.Gray, fontSize = 14.sp)
                    Text(text = entry.password, color = Color.Gray, fontSize = 14.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Text("❌", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AddEntryDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Login") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g. Netflix)") })
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, username, password) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}