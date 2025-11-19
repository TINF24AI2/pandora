package app.pandorapass.pandora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

data class Password(val url: String, val username: String, val password: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Passwords(modifier: Modifier) {
    var query: String by remember { mutableStateOf("") }
    var passwords: List<Password> by remember { mutableStateOf(listOf()) }
    var addPassword by remember { mutableStateOf(false) }

    val filteredPasswords = passwords.filter {
        it.url.contains(query, ignoreCase = true) or
                it.username.contains(query, ignoreCase = true)
    }

    Scaffold(modifier = modifier, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                addPassword = true
            }
        ) {
            Icon(ImageVector.vectorResource(R.drawable.plus_24_outlined), "Add login credentials",  tint = MaterialTheme.colorScheme.onPrimary)
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                modifier = Modifier.align(Alignment.CenterHorizontally),
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
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredPasswords) { password ->
                    PasswordItem(Modifier.padding(top = 10.dp), password)
                }
            }
        }
    }
    if (addPassword) {
        AddPassword({ addPassword = false },
            { newPassword: Password -> passwords = passwords + newPassword})
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
        containerColor = Color(0, 0, 0)
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
                OutlinedTextField(
                    modifier = width,
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    placeholder = { Text("Username") },
                    singleLine = true
                )
                Row(
                    modifier = width.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        visualTransformation = if (showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                        placeholder = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).fillMaxHeight()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.2f)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .clickable { showPassword = !showPassword },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(ImageVector.vectorResource(
                            if (showPassword) R.drawable.eye_24_outlined
                            else R.drawable.eye_slash_24_outlined),
                            "show Password",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                OutlinedTextField(
                    modifier = width,
                    value = newURL,
                    onValueChange = { newURL = it },
                    placeholder = { Text("URL") },
                    singleLine = true
                )
                Button(modifier = width, onClick = { if (newURL.isNotBlank() and newUsername.isNotBlank() and newPassword.isNotBlank()) addNewPassword(Password(newURL, newUsername, newPassword)) }) {
                    Text("Add Password")
                }
            }
        }
    }
}

@Composable
fun PasswordItem(modifier: Modifier, password: Password) {
    Column(modifier = modifier) {
        Text(password.url)
        Text(password.username)
    }
}