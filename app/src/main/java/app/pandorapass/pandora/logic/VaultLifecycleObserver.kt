package app.pandorapass.pandora.logic

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import app.pandorapass.pandora.PandoraApplication
import app.pandorapass.pandora.data.SettingsDataStore
import app.pandorapass.pandora.logic.services.VaultSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class VaultLifecycleObserver(private val context: Context) : DefaultLifecycleObserver {
    private val settingsDataStore = SettingsDataStore(context)
    private var backgroundedTime: Date? = null

    override fun onStart(owner: LifecycleOwner) {
        // App is in foreground
        if (backgroundedTime != null) {
            val now = Date()
            val diff = now.time - backgroundedTime!!.time

            CoroutineScope(Dispatchers.Main).launch {
                val autoLockTimeout = settingsDataStore.autoLockTimeout.first()

                if (autoLockTimeout == 0) {
                    VaultSession.clear()
                    (context.applicationContext as? PandoraApplication)?.triggerLockEvent()
                } else if (autoLockTimeout > 0) {
                    val autoLockTimeoutMs = autoLockTimeout * 1000L
                    if (diff > autoLockTimeoutMs) {
                        VaultSession.clear()
                        (context.applicationContext as? PandoraApplication)?.triggerLockEvent()
                    }
                }

                backgroundedTime = null
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App is in background
        backgroundedTime = Date()
    }
}