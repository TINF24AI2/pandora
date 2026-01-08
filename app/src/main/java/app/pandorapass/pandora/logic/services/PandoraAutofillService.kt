package app.pandorapass.pandora.logic.services

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.widget.RemoteViews
import androidx.autofill.inline.v1.InlineSuggestionUi
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.logic.utils.ResponseBuilderHelper
import app.pandorapass.pandora.logic.utils.StructureParser.parseStructure
import app.pandorapass.pandora.ui.activities.AutofillAuthActivity
import kotlinx.coroutines.runBlocking

class PandoraAutofillService : AutofillService() {
    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val context = request.fillContexts.last()
        val vaultService = (application as PandoraApplication).vaultService

        val structure = context.structure
        val parsed = parseStructure(structure)

        if (parsed.usernameId == null && parsed.passwordId == null) {
            callback.onSuccess(null)
            return
        }

        val isVaultInitialized = runBlocking {
            vaultService.isVaultInitialized()
        }

        if (!isVaultInitialized) {
            callback.onSuccess(null)
            return
        }

        // If the vault is already unlocked, we can show the entries.
        if (VaultSession.isVaultUnlocked()) {
            val accounts = vaultService.entries.value.asSequence()
                .filterIsInstance<LoginVaultEntry>()
                .map { entry ->
                    val displayTitle = entry.title.ifBlank { "Untitled" }
                    AutofillAuthActivity.Account(
                        displayTitle,
                        entry.username,
                        entry.password
                    )
                }
                .distinctBy { "${it.label}|${it.username}" } // Prevent Duplicates
                .toList()

            val response = ResponseBuilderHelper.buildResponse(
                context = this,
                accounts = accounts,
                usernameId = parsed.usernameId,
                passwordId = parsed.passwordId,
                inlineRequest = request.inlineSuggestionsRequest
            )

            callback.onSuccess(response)
            return
        }

        val authIntent = Intent(this, AutofillAuthActivity::class.java).apply {
            putExtra(AutofillAuthActivity.EXTRA_USERNAME_ID, parsed.usernameId)
            putExtra(AutofillAuthActivity.EXTRA_PASSWORD_ID, parsed.passwordId)
            putExtra(AutofillAuthActivity.EXTRA_INLINE_REQUEST, request.inlineSuggestionsRequest)
        }

        val intentSender = PendingIntent.getActivity(
            this,
            1001,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).intentSender

        // Fallback Dropdown UI if the inline representation doesn't work for some reason
        val dropdownPresentation = RemoteViews(packageName, R.layout.simple_list_item_1)
        dropdownPresentation.setTextViewText(R.id.text1, "Unlock Vault")

        // Inline Presentation
        val inlinePresentation = createInlinePresentation(request, "Unlock Vault")

        // The final data set
        val datasetBuilder: Dataset.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val presentationsBuilder = Presentations.Builder()
                .setMenuPresentation(dropdownPresentation) // standard dropdown
                .setDialogPresentation(dropdownPresentation) // dialog UI (if supported)

            if (inlinePresentation != null) {
                presentationsBuilder.setInlinePresentation(inlinePresentation)
            }

            datasetBuilder = Dataset.Builder(presentationsBuilder.build())

        } else {
            datasetBuilder = Dataset.Builder(dropdownPresentation)

            if (inlinePresentation != null) {
                datasetBuilder.setInlinePresentation(inlinePresentation)
            }
        }

        datasetBuilder.setAuthentication(intentSender)

        // Set Fields
        parsed.usernameId?.let { datasetBuilder.setValue(it, null) }
        parsed.passwordId?.let { datasetBuilder.setValue(it, null) }

        // 6. Return Response
        val response = FillResponse.Builder()
            .addDataset(datasetBuilder.build())
            .build()

        callback.onSuccess(response)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // We don't support direct saving via the popup yet.
        callback.onSuccess()
    }

    @SuppressLint("RestrictedApi")
    private fun createInlinePresentation(request: FillRequest, text: String): InlinePresentation? {
        val inlineRequest = request.inlineSuggestionsRequest ?: return null
        val styles = inlineRequest.inlinePresentationSpecs.firstOrNull() ?: return null

        val pendingIntent =
            PendingIntent.getActivity(this, text.hashCode(), Intent(), PendingIntent.FLAG_IMMUTABLE)
        val slice = InlineSuggestionUi.newContentBuilder(pendingIntent)
            .setTitle(text)
            .build()

        return InlinePresentation(slice.slice, styles, false)
    }
}