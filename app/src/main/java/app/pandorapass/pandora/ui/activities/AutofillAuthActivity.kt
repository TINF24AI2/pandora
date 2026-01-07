package app.pandorapass.pandora.ui.activities

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.BiometricTokenStorage
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.logic.services.VaultService
import app.pandorapass.pandora.ui.pages.LoginView
import app.pandorapass.pandora.ui.pages.PandoraApp
import app.pandorapass.pandora.ui.viewmodels.AppState
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModel
import app.pandorapass.pandora.ui.viewmodels.TestVaultViewModelFactory

class AutofillAuthActivity : FragmentActivity() {

    companion object {
        const val EXTRA_USERNAME_ID = "extra_username_id"
        const val EXTRA_PASSWORD_ID = "extra_password_id"
        const val EXTRA_INLINE_REQUEST = "extra_inline_request"
    }

    private var usernameId: AutofillId? = null
    private var passwordId: AutofillId? = null
    private var inlineRequest: InlineSuggestionsRequest? = null

    private var vaultService: VaultService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usernameId = intent.getParcelableExtra(EXTRA_USERNAME_ID)
        passwordId = intent.getParcelableExtra(EXTRA_PASSWORD_ID)
        inlineRequest = intent.getParcelableExtra(EXTRA_INLINE_REQUEST)

        vaultService = (application as PandoraApplication).vaultService

        setContent {
            val factory = TestVaultViewModelFactory(vaultService!!)
            val viewModel: TestVaultViewModel = viewModel(factory = factory)

            val context = LocalContext.current
            val biometricCryptoHelper = (application as PandoraApplication).biometricCryptoHelper

            val appState by viewModel.appState.collectAsState()
            val error by viewModel.error.collectAsState()

            val biometricStorage = BiometricTokenStorage(context)
            if (biometricStorage.isBiometricEnabled() && appState == AppState.LOCKED) {
                biometricCryptoHelper.showBiometricUnlock(
                    activity = this,
                    application = application as PandoraApplication,
                    onSuccess = { decryptedMasterKey ->
                        run {
                            viewModel.unlockVaultWithKey(decryptedMasterKey) {
                                onSuccess()
                            }
                        }
                    }
                )

                return@setContent
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red)
                        .padding(8.dp)
                        .zIndex(1f)
                        .statusBarsPadding()
                )
            }

            LoginView(
                onSubmit = {
                    viewModel.unlockVaultWithPassword(it) { onSuccess() }
                }
            )
        }
    }

    private fun onSuccess() {
        val accounts: List<Account> = vaultService?.entries?.value?.map { entry ->
            if (entry is LoginVaultEntry) {
                Account(entry.title, entry.username, entry.password)
            } else {
                Account("Unknown", "", "")
            }
        }?.ifEmpty {
            listOf(Account("No logins found", "", ""))
        } ?: listOf(Account("No logins found", "", ""))

        val responseBuilder = FillResponse.Builder()

        for (account in accounts) {
            // 1. Create Dropdown Presentation (Fallback)
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            presentation.setTextViewText(android.R.id.text1, account.label)

            // 2. Create Dataset
            val datasetBuilder = Dataset.Builder(presentation)

            // 3. Create Inline Presentation (Keyboard Strip)
            if (inlineRequest != null) {
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

        val resultIntent = Intent()
        resultIntent.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, responseBuilder.build())
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @SuppressLint("RestrictedApi")
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
