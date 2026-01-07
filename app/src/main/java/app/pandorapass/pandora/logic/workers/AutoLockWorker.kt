package app.pandorapass.pandora.logic.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.pandorapass.pandora.PandoraApplication
import android.util.Log
class AutoLockWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        const val WORK_NAME = "autoLockWork"
        const val TAG = "AutoLockWorker"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as? PandoraApplication
        if (app == null) {
            Log.e(
                TAG,
                "Application context is not PandoraApplication. Actual type: " +
                        applicationContext::class.java.name
            )
            return Result.failure()
        }
        return try {
            app.triggerLockEvent()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error while executing AutoLockWorker", e)
            Result.failure()
        }
    }
}