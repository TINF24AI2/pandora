package app.pandorapass.pandora.ui.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.pandorapass.pandora.ui.theme.PandoraTheme

class AutofillAuthActivity : ComponentActivity() {

    companion object {
        const val EXTRA_USERNAME_ID = "extra_username_id"
        const val EXTRA_PASSWORD_ID = "extra_password_id"
        const val EXTRA_INLINE_REQUEST = "extra_inline_request"
    }

    private var usernameId: AutofillId? = null
    private var passwordId: AutofillId? = null
    private var inlineRequest: InlineSuggestionsRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usernameId = intent.getParcelableExtra(EXTRA_USERNAME_ID)
        passwordId = intent.getParcelableExtra(EXTRA_PASSWORD_ID)
        inlineRequest = intent.getParcelableExtra(EXTRA_INLINE_REQUEST)

        setContent {
            // Simple Unlock UI
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter PIN: 1234")
                Button(onClick = { onSuccess() }) {
                    Text("Unlock")
                }
            }
        }
    }

    private fun onSuccess() {
        val accounts = listOf(
            Account("Personal", "me@gmail.com", "pass1"),
            Account("Work", "admin@corp.com", "pass2")
        )

        val responseBuilder = FillResponse.Builder()

        for (account in accounts) {
            // 1. Create Dropdown Presentation (Fallback)
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            presentation.setTextViewText(android.R.id.text1, account.label)

            // 2. Create Dataset
            val datasetBuilder = Dataset.Builder(presentation)

            // 3. Create Inline Presentation (Keyboard Strip)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && inlineRequest != null) {
                val inline = createInline(account.label, account.username)
                if (inline != null) {
                    datasetBuilder.setInlinePresentation(inline)
                }
            }

            // 4. Set Values
            usernameId?.let { datasetBuilder.setValue(it, AutofillValue.forText(account.username)) }
            passwordId?.let { datasetBuilder.setValue(it, AutofillValue.forText(account.password)) }

            responseBuilder.addDataset(datasetBuilder.build())
        }

        // NOTE: We do NOT set SaveInfo here, ensuring no "Save?" prompt appears.

        val resultIntent = Intent()
        resultIntent.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, responseBuilder.build())
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createInline(title: String, subtitle: String): InlinePresentation? {
        val request = inlineRequest ?: return null
        val styles = request.inlinePresentationSpecs.firstOrNull() ?: return null

        // Placeholder intent (required by API but not used for direct filling)
        val pIntent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)

        val slice = InlineSuggestionUi.newContentBuilder(pIntent)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()

        return InlinePresentation(slice.slice, styles, false)
    }

    data class Account(val label: String, val username: String, val password: String)
}

// --- Composable UI ---
@Composable
fun AuthScreen(onUnlockAttempt: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unlock Password Vault",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4) pin = it }, // Limit to 4 chars
            label = { Text("Enter PIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onUnlockAttempt(pin) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }
    }
}