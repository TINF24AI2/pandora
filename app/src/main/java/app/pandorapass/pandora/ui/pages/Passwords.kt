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

data class Password(val url: String, val username: String, val password: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPage(modifier: Modifier) {
    var query: String by remember { mutableStateOf("") }
    var passwords: List<Password> by remember { mutableStateOf(listOf()) }
    var addPassword by remember { mutableStateOf(false) }
    var showPasswordEntry by remember { mutableStateOf(false) }
    var shownPassword: Password? by remember { mutableStateOf(null) }

    val filteredPasswords = passwords.filter {
        it.url.contains(query, ignoreCase = true) or
                it.username.contains(query, ignoreCase = true)
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
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredPasswords) { password ->
                    PasswordItem(password =  password, showEntry = { showPasswordEntry = true; shownPassword = password })
                }
            }
        }
    }
    if (addPassword) {
        AddPassword(
            { addPassword = false },
            { newPassword: Password -> passwords = passwords + newPassword })
    }
    if (showPasswordEntry) {
        ShowEntry(shownPassword, { showPasswordEntry = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyableTextField(
    modifier: Modifier = Modifier,
    label: String,
    text: String
) {
    val clipboard: ClipboardManager = LocalContext.current.getSystemService(ClipboardManager::class.java)
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
fun ShowEntry(password: Password?, onDismiss: () -> Unit) {
    val curPassword = password
    if (curPassword == null) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(),
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
                Text("Password Details")
                    CopyableTextField(label = "Username", text = curPassword.username)
                    CopyableTextField(label = "Password", text = curPassword.password)
                    CopyableTextField(label = "URL", text = curPassword.url)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassword(onDismiss: () -> Unit, addNewPassword: (Password) -> Unit) {
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newURL by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = rememberModalBottomSheetState(),
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
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val width = Modifier.fillMaxWidth()
                Text("Add Password")
                OutlinedTextField(
                    modifier = width,
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    singleLine = true
                )
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
                OutlinedTextField(
                    modifier = width,
                    value = newURL,
                    onValueChange = { newURL = it },
                    label = { Text("URL") },
                    singleLine = true
                )
                Button(
                    modifier = width,
                    onClick = {
                        if (newURL.isNotBlank() and newUsername.isNotBlank() and newPassword.isNotBlank()) addNewPassword(
                            Password(newURL, newUsername, newPassword)
                        )
                        onDismiss()
                    }) {
                    Text("Add Password")
                }
            }
        }
    }
}

@Composable
fun PasswordItem(modifier: Modifier = Modifier, password: Password, showEntry: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { showEntry() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(password.url)
            Text(password.username)
        }
    }
}