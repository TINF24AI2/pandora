package app.pandorapass.pandora.ui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.pandorapass.pandora.R
import app.pandorapass.pandora.ui.viewmodels.SettingsViewModel
import app.pandorapass.pandora.ui.viewmodels.VaultViewModel

@Composable
fun PandoraApp(
    startPage: String,
    viewModel: VaultViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController()
) {
    // Get current route from NavController's back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Map routes to AppDestinations
    val currentDestination = AppDestinations.fromRoute(currentRoute)
    
    val myNavigationSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            unselectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(if (destination == currentDestination) destination.selectedIconRes else destination.iconRes),
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = {
                        navController.navigate(destination.route) {
                            // Avoid multiple copies of the same destination when reselecting the same item
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Restore state when reselecting a previously selected item
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = myNavigationSuiteItemColors
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (startPage == "settings") AppDestinations.SETTINGS.route else AppDestinations.PASSWORDS.route
            ) {
                composable(AppDestinations.PASSWORDS.route) {
                    PasswordPage(Modifier.padding(innerPadding), viewModel, settingsViewModel)
                }
                composable(AppDestinations.GENERATE.route) {
                    GeneratePage(Modifier.padding(innerPadding), settingsViewModel)
                }
                composable(AppDestinations.SETTINGS.route) {
                    SettingsPage(Modifier.padding(innerPadding), viewModel, settingsViewModel)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val iconRes: Int,
    val selectedIconRes: Int,
    val route: String
) {
    PASSWORDS("Passwords", R.drawable.folder_24_outlined, R.drawable.folder_24_filled, "passwords"),
    GENERATE("Generate", R.drawable.sparkles_24_outline, R.drawable.sparkles_24_filled, "generate"),
    SETTINGS("Settings", R.drawable.settings_24_outline, R.drawable.settings_24_filled, "settings");
    
    companion object {
        fun fromRoute(route: String?): AppDestinations {
            return entries.find { it.route == route } ?: PASSWORDS
        }
    }
 }