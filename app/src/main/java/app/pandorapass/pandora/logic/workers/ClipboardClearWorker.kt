package app.pandorapass.pandora.logic.workers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClipboardClearWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "clipboardClearWork"
    }

    override fun doWork(): Result {

        try {
            val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val emptyClip = ClipData.newPlainText("", "")
            clipboardManager.setPrimaryClip(emptyClip)

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}
