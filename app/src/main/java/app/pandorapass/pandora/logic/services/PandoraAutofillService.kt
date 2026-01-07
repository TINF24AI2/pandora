package app.pandorapass.pandora.logic.services

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
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.logic.models.LoginVaultEntry
import app.pandorapass.pandora.logic.utils.ResponseBuilderHelper
import app.pandorapass.pandora.logic.utils.StructureParser.parseStructure
import app.pandorapass.pandora.ui.activities.AutofillAuthActivity

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

        val usernameId = parsed.usernameId
        val passwordId = parsed.passwordId

        if (VaultSession.isVaultUnlocked()) {
            val accounts = vaultService.entries.value.map { entry ->
                if (entry is LoginVaultEntry) AutofillAuthActivity.Account(
                    entry.title,
                    entry.username,
                    entry.password
                )
                else AutofillAuthActivity.Account("Unknown", "", "")
            }

            val response = ResponseBuilderHelper.buildResponse(
                context = this,
                accounts = accounts,
                usernameId = usernameId,
                passwordId = passwordId,
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
        val dropdownPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        dropdownPresentation.setTextViewText(android.R.id.text1, "Unlock Vault")

        // Inline Presentation
        var inlinePresentation: InlinePresentation? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            inlinePresentation = createInlinePresentation(request, "Unlock Vault")
        }

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
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createInlinePresentation(request: FillRequest, text: String): InlinePresentation? {
        val inlineRequest = request.inlineSuggestionsRequest ?: return null
        val styles = inlineRequest.inlinePresentationSpecs.firstOrNull() ?: return null

        val pendingIndent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
        val slice = InlineSuggestionUi.newContentBuilder(pendingIndent)
            .setTitle(text)
            .build()

        return InlinePresentation(slice.slice, styles, false)
    }
}