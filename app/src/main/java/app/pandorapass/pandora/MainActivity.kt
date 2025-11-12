package app.pandorapass.pandora

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.pandorapass.pandora.ui.theme.PandoraTheme
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore("user_prefs")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PandoraTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "login") {
                    composable("login") { Login(navController) }
                    composable("pandora") { PandoraApp(navController) }
                }
            }
        }
    }
}

@Composable
fun Login(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usernameKey = stringPreferencesKey("username")
    var username by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        username = prefs[usernameKey]
    }

    if (username != null) {
        OldUserLogin(username!!, navController)
    }
    else {
        NewUserLogin(navController) { newUsername ->
            scope.launch {
                context.dataStore.edit { prefs ->
                    prefs[usernameKey] = newUsername
                }
            }
        }
    }
}
@Composable
fun OldUserLogin(username: String, navController: NavController) {
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .safeContentPadding())
    { innerPadding ->
        Button(onClick = { navController.navigate("pandora") }, Modifier.padding(innerPadding)) { Text("login") }
    }
}

@Composable
fun NewUserLogin(navController: NavController, onLogin: (username: String) -> Unit) {
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .safeContentPadding())
    { innerPadding ->
        var username by remember { mutableStateOf("") }

        Scaffold(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Enter username") }
                )
                Button(
                    onClick = { if (username.isNotBlank()) {
                        onLogin(username)
                        navController.navigate("pandora")
                    } }
                ) {
                    Text("Save and Continue")
                }
            }
        }
    }
}