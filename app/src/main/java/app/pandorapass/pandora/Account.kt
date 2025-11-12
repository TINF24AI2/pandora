package app.pandorapass.pandora

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun Account(modifier: Modifier, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usernameKey = stringPreferencesKey("username")

    Text("Account", modifier = modifier)

    Button(
        onClick = {
            scope.launch {
                context.dataStore.edit { prefs ->
                    prefs.remove(usernameKey)
                }
                navController.navigate("login")
            }
        },
        content = { Text("Logout") }
    )
}
