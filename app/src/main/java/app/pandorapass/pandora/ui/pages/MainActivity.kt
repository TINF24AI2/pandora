package app.pandorapass.pandora.ui.pages

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import app.pandorapass.pandora.logic.models.FileVaultRepository
import app.pandorapass.pandora.logic.services.impl.CryptoServiceImpl
import app.pandorapass.pandora.logic.services.impl.VaultServiceImpl
import app.pandorapass.pandora.ui.theme.PandoraTheme
import app.pandorapass.pandora.ui.viewmodels.AppState
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModelFactory

class MainActivity : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PandoraTheme {
                val repository = FileVaultRepository(applicationContext)
                val cryptoService = CryptoServiceImpl()
                val vaultService = VaultServiceImpl(cryptoService, repository)

                val factory = TestVaultViewModelFactory(vaultService)
                val viewModel: TestVaultViewModel = viewModel(factory = factory)
                val appState by viewModel.appState.collectAsState()
                val error by viewModel.error.collectAsState()

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(8.dp)
                            .zIndex(1f)
                    )
                }

                when (appState) {
                    AppState.LOADING -> CircularProgressIndicator()

                    AppState.SETUP -> Login(
                        onSubmit = { viewModel.createVault(it) }
                    )

                    AppState.LOCKED -> Login(
                        onSubmit = { viewModel.unlockVault(it) }
                    )

                    AppState.UNLOCKED -> PandoraApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun Login(onSubmit: (pass: String) -> Unit) {
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                )
                Button(
                    onClick = {
                        onSubmit(password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                //optional bottom text. leave for now
                Text(
                    text = "By continuing, you agree to our Terms & Conditions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}