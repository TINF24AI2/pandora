package app.pandorapass.pandora.logic.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import app.pandorapass.pandora.ui.activities.AutofillAuthActivity
import java.util.concurrent.atomic.AtomicInteger

object ResponseBuilderHelper {
    // FIX 2: Atomic Counter for unique Request Codes
    // This prevents the OS from confusing different slices or dropping them.
    private val requestCodeCounter = AtomicInteger(1000)

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

            // 1. Prepare Dropdown View
            val dropdownPresentation = RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, account.label)
            }

            // 2. Prepare Inline View
            var inlinePresentation: InlinePresentation? = null
            if (inlineRequest != null) {
                val inlineTitle = if (account.label == "Vault Empty") "Empty" else account.label
                val inlineSubtitle = if (account.label == "Vault Empty") "No items" else account.username
                inlinePresentation = createInline(context, inlineRequest, inlineTitle, inlineSubtitle)
            }

            val datasetBuilder: Dataset.Builder

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ (Android 13)
                val presentationsBuilder = Presentations.Builder()
                    .setMenuPresentation(dropdownPresentation)
                    .setDialogPresentation(dropdownPresentation)

                if (inlinePresentation != null) {
                    presentationsBuilder.setInlinePresentation(inlinePresentation)
                }

                datasetBuilder = Dataset.Builder(presentationsBuilder.build())

            } else {
                // API < 33 (Android 12)
                // If we have a valid inline presentation, we construct the builder with the dropdown
                // but immediately attach the inline version.
                datasetBuilder = Dataset.Builder(dropdownPresentation)

                if (inlinePresentation != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    datasetBuilder.setInlinePresentation(inlinePresentation)
                }
            }

            // 3. Set Values
            if (account.label != "Vault Empty") {
                val uValue = if (account.username.isNotEmpty()) AutofillValue.forText(account.username) else null
                val pValue = if (account.password.isNotEmpty()) AutofillValue.forText(account.password) else null

                if (usernameId != null && uValue != null) {
                    datasetBuilder.setValue(usernameId, uValue)
                }
                if (passwordId != null && pValue != null) {
                    datasetBuilder.setValue(passwordId, pValue)
                }
            }

            responseBuilder.addDataset(datasetBuilder.build())
        }

        return responseBuilder.build()
    }

    @SuppressLint("RestrictedApi")
    private fun createInline(
        context: Context,
        inlineRequest: InlineSuggestionsRequest,
        title: String,
        subtitle: String
    ): InlinePresentation? {

        val validSpec = inlineRequest.inlinePresentationSpecs.firstOrNull { spec ->
            UiVersions.getVersions(spec.style).contains(UiVersions.INLINE_UI_VERSION_1)
        } ?: return null

        val intent = Intent() // Intent is unused for filling, but required for the builder

        val uniqueRequestCode = requestCodeCounter.getAndIncrement()

        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val content = InlineSuggestionUi.newContentBuilder(pendingIntent)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()

        return InlinePresentation(
            content.slice,
            validSpec,
            true // Pin to start
        )
    }
}