package app.pandorapass.pandora.ui.pages

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel

@Composable
fun AccountPage(modifier: Modifier, viewModel: TestVaultViewModel) {

    Text("Account", modifier = modifier)

    Button(
        onClick = {
            viewModel.lockVault()
        },
        content = { Text("Logout") }
    )
}
