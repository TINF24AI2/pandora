package app.pandorapass.pandora.ui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.pandorapass.pandora.R
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel

@Composable
fun PandoraApp(
    startPage: String,
    viewModel: TestVaultViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PASSWORDS) }
    val myNavigationSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(if (it == currentDestination) it.selectedIconRes else it.iconRes),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                    colors = myNavigationSuiteItemColors
                )
            }
        }
    ) {

            // Define your NavHost
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {  PasswordPage(Modifier.safeContentPadding(), viewModel) }
                composable("settings") { SettingsPage(Modifier.safeContentPadding(), viewModel, settingsViewModel) }
                // ... other destinations
            }

            // THE MAGIC MOMENT:
            // This block runs immediately after the NavHost is composed.
            LaunchedEffect(Unit) {
                if (startPage == "settings") {
                    navController.navigate("settings") {
                        // Optional: Clears back stack so "Back" goes to Home, not exits app
                        popUpTo("home")
                    }
                }
            }


            Scaffold(modifier = Modifier.fillMaxSize().safeContentPadding()) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.PASSWORDS -> PasswordPage(Modifier.padding(innerPadding), viewModel)
                    AppDestinations.GENERATE -> GeneratePage(Modifier.padding(innerPadding))
                    AppDestinations.SETTINGS -> SettingsPage(Modifier.padding(innerPadding), viewModel, settingsViewModel)
                }
            }

    }
}

enum class AppDestinations(
    val label: String,
    val iconRes: Int,
    val selectedIconRes: Int
) {
    PASSWORDS("Passwords", R.drawable.folder_24_outlined, R.drawable.folder_24_filled),
    GENERATE("Generate", R.drawable.sparkles_24_outline, R.drawable.sparkles_24_filled),
    SETTINGS("Settings", R.drawable.settings_24_outline, R.drawable.settings_24_filled),
 }