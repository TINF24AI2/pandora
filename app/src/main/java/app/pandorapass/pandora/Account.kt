package app.pandorapass.pandora

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun Account(modifier: Modifier, navController: NavHostController) {

    Text("Account", modifier = modifier)

    Button(
        onClick = {
            navController.navigate("login")
        },
        content = { Text("Logout") }
    )
}
