package app.pandorapass.pandora

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


data class Password(val url: String, val username: String, val password: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Passwords(modifier: Modifier) {
    var query: String by remember { mutableStateOf("") }
    var passwords: List<Password> by remember { mutableStateOf(listOf(
        Password("testurl1", "testusername1", "testpassword1"),
        Password("testurl2", "testusername2", "testpassword2"),
        Password("testurl3", "testusername3", "testpassword3"),
        Password("testurl4", "testusername4", "testpassword4")
    )) }

    val filteredPasswords = passwords.filter {
        it.url.contains(query, ignoreCase = true) or
        it.username.contains(query, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                Text(password.url) //TODO replace Composable that allows clicking/...
            }
        }
    }
}