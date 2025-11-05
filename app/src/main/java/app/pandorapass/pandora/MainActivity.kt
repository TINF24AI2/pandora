package app.pandorapass.pandora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import app.pandorapass.pandora.ui.theme.PandoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PandoraTheme {
                PandoraApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PandoraApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PASSWORDS) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(it.iconRes),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        },
        content = {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.PASSWORDS -> Passwords(Modifier.padding(innerPadding))
                    AppDestinations.GENERATE -> Generate(Modifier.padding(innerPadding))
                    AppDestinations.SETTINGS -> Settings(Modifier.padding(innerPadding))
                    AppDestinations.ACCOUNT -> Account(Modifier.padding(innerPadding))
                }
            }
        }
    )
}

enum class AppDestinations(
    val label: String,
    val iconRes: Int,
) {
    PASSWORDS("Passwords", R.drawable.folder_24_outlined),
    GENERATE("Generate", R.drawable.outline_build_24),
    SETTINGS("Settings", R.drawable.outline_settings_24),
    ACCOUNT("Account", R.drawable.outline_account_circle_24),
}