package app.pandorapass.pandora.logic.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.pandorapass.pandora.PandoraApplication

class AutoLockWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "autoLockWork"
    }

    override suspend fun doWork(): Result {
        return try {
            // Get the application instance and trigger the lock event.
            (applicationContext as PandoraApplication).triggerLockEvent()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
