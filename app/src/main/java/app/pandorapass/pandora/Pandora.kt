package app.pandorapass.pandora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController



@Composable
fun PandoraApp(navController: NavHostController) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PASSWORDS) }
    val myNavigationSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            unselectedIconColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
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
        },
        content = {
            Scaffold(modifier = Modifier.fillMaxSize().safeContentPadding(), floatingActionButton = {
                FloatingActionButton(
                    onClick = {

                    }
                ) {
                    Icon(Icons.Filled.Add, "Add login credentials")
                }
            }) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.PASSWORDS -> Passwords(Modifier.padding(innerPadding))
                    AppDestinations.GENERATE -> Generate(Modifier.padding(innerPadding))
                    AppDestinations.SETTINGS -> Settings(Modifier.padding(innerPadding))
                    AppDestinations.ACCOUNT -> Account(Modifier.padding(innerPadding), navController)
                }
            }
        }
    )
}

enum class AppDestinations(
    val label: String,
    val iconRes: Int,
    val selectedIconRes: Int
) {
    PASSWORDS("Passwords", R.drawable.folder_24_outlined, R.drawable.folder_24_filled),
    GENERATE("Generate", R.drawable.sparkles_24_outline, R.drawable.sparkles_24_filled),
    SETTINGS("Settings", R.drawable.settings_24_outline, R.drawable.settings_24_filled),
    ACCOUNT("Account", R.drawable.account_circle_24_outline, R.drawable.account_circle_24_filled),
}