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
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import app.pandorapass.pandora.logic.utils.StructureParser.parseStructure
import app.pandorapass.pandora.ui.activities.AutofillAuthActivity

class PandoraAutofillService : AutofillService() {
    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val context = request.fillContexts.last()
        val structure = context.structure
        val parsed = parseStructure(structure)

        if (parsed.usernameId == null && parsed.passwordId == null) {
            callback.onSuccess(null)
            return
        }

        // 1. Prepare Auth Intent
        val authIntent = Intent(this, AutofillAuthActivity::class.java).apply {
            putExtra(AutofillAuthActivity.EXTRA_USERNAME_ID, parsed.usernameId)
            putExtra(AutofillAuthActivity.EXTRA_PASSWORD_ID, parsed.passwordId)

            // Clean API check for the Inline Request
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                putExtra(AutofillAuthActivity.EXTRA_INLINE_REQUEST, request.inlineSuggestionsRequest)
            }
        }

        val intentSender = PendingIntent.getActivity(
            this,
            1001,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).intentSender

        // 2. Dropdown UI
        val dropdownPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        dropdownPresentation.setTextViewText(android.R.id.text1, "Unlock Vault")

        // 3. Inline UI (API 30+)
        var inlinePresentation: InlinePresentation? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            inlinePresentation = createInlinePresentation(request, "Unlock Vault")
        }

        // 4. Build Dataset (The Correct Way)
        val datasetBuilder = Dataset.Builder(dropdownPresentation)

        if (inlinePresentation != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            datasetBuilder.setInlinePresentation(inlinePresentation)
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
        // Implementation for manual saves (as discussed previously)
        callback.onSuccess()
    }

    // --- Inline UI Helper ---
    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun createInlinePresentation(request: FillRequest, text: String): InlinePresentation? {
        val inlineRequest = request.inlineSuggestionsRequest ?: return null
        val styles = inlineRequest.inlinePresentationSpecs.firstOrNull() ?: return null

        // Define the Intent that fires when the user taps the chip (same as the auth intent generally)
        // For the "Unlock" button, the action is handled by the Dataset's authentication,
        // so this pending intent is just a placeholder to satisfy the builder.
        val pIntent = PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)

        val slice = InlineSuggestionUi.newContentBuilder(pIntent)
            .setTitle(text)
            .build()

        return InlinePresentation(slice.slice, styles, false)
    }
}