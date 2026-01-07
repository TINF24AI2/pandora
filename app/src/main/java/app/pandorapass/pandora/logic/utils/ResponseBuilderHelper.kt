package app.pandorapass.pandora.logic.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import app.pandorapass.pandora.ui.activities.AutofillAuthActivity

object ResponseBuilderHelper {
    /**
     * Builds a FillResponse containing datasets for the given accounts.
     * Can be called from both the Auth Activity and the AutofillService.
     */
    fun buildResponse(
        context: Context,
        accounts: List<AutofillAuthActivity.Account>,
        usernameId: AutofillId?,
        passwordId: AutofillId?,
        inlineRequest: InlineSuggestionsRequest?
    ): FillResponse {

        val responseBuilder = FillResponse.Builder()
        val safeAccounts = accounts.ifEmpty {
            listOf(AutofillAuthActivity.Account("Vault Empty", "", ""))
        }

        for (account in safeAccounts) {
            val datasetBuilder = Dataset.Builder()

            // --- Dropdown Presentation ---
            val dropdownPresentation =
                RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
                    setTextViewText(android.R.id.text1, account.label)
                }

            // We need to attach the presentation to a specific field ID.
            // Usually, we attach it to the username field, or the password field if username is missing.
            val presentationId = usernameId ?: passwordId
            if (presentationId != null) {
                // Determine the value to fill. If it's the "Vault Empty" placeholder, we might fill nothing or clear it.
                val fillValue =
                    if (account.label == "Vault Empty") null else AutofillValue.forText(account.username)

                // Set the presentation for the main field
                datasetBuilder.setValue(presentationId, fillValue, dropdownPresentation)
            }

            // --- 2. Inline Presentation (Keyboard Strip) ---
            if (inlineRequest != null) {
                val inlineTitle = if (account.label == "Vault Empty") "Empty" else account.label
                val inlineSubtitle =
                    if (account.label == "Vault Empty") "No items" else account.username

                val inlinePresentation =
                    createInline(context, inlineRequest, inlineTitle, inlineSubtitle)

                if (inlinePresentation != null && presentationId != null) {
                    datasetBuilder.setInlinePresentation(inlinePresentation, inlinePresentation)
                }
            }

            // We skip setting values if it's the "Vault Empty" placeholder to avoid overwriting user text
            if (account.label != "Vault Empty") {
                if (usernameId != null && account.username.isNotEmpty()) {
                    datasetBuilder.setValue(usernameId, AutofillValue.forText(account.username))
                }
                if (passwordId != null && account.password.isNotEmpty()) {
                    datasetBuilder.setValue(passwordId, AutofillValue.forText(account.password))
                }
            }

            responseBuilder.addDataset(datasetBuilder.build())
        }

        return responseBuilder.build()
    }

    /**
     * Helper to create the Slice for the keyboard suggestion strip.
     * Uses androidx libraries to handle the UI styling automatically.
     */
    private fun createInline(
        context: Context,
        inlineRequest: InlineSuggestionsRequest,
        title: String,
        subtitle: String
    ): InlinePresentation? {
        // Iterate through the specs provided by the keyboard (Gboard, Samsung Keyboard, etc.)
        // We look for the first spec that supports "Version 1" (the standard Android UI style)
        val validSpec = inlineRequest.inlinePresentationSpecs.firstOrNull { spec ->
            UiVersions.getVersions(spec.style).contains(UiVersions.INLINE_UI_VERSION_1)
        } ?: return null

        // Create the PendingIntent (required by API)
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val content = InlineSuggestionUi.newContentBuilder(pendingIntent)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()

        // 4. Return the presentation
        // crucial: We must pass back the specific 'validSpec' we found earlier
        return InlinePresentation(
            content.slice,
            validSpec,
            true
        )
    }
}