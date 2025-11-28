package app.pandorapass.pandora.ui.pages

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel

@Composable
<<<<<<<< HEAD:app/src/main/java/app/pandorapass/pandora/ui/pages/Account.kt
fun AccountPage(modifier: Modifier, viewModel: TestVaultViewModel) {
========
fun AccountPage(modifier: Modifier, navController: NavHostController) {
>>>>>>>> origin/feat/view-password:app/src/main/java/app/pandorapass/pandora/AccountPage.kt

    Text("Account", modifier = modifier)

    Button(
        onClick = {
            viewModel.lockVault()
        },
        content = { Text("Logout") }
    )
}
