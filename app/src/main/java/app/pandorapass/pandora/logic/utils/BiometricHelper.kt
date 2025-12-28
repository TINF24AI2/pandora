package app.pandorapass.pandora.logic.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("BiometricAuth", "No biometric hardware available.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("BiometricAuth", "Biometric hardware unavailable.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("BiometricAuth", "No biometrics enrolled.")
                false
            }

            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun promptEnrollBiometric(context: Context) {
        try {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
            }
            if (context is FragmentActivity) {
                context.startActivity(enrollIntent)
            }
        } catch (e: ActivityNotFoundException) {
            // Fallback to a more generic settings screen
            val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            if (context is FragmentActivity) {
                context.startActivity(fallbackIntent)
            }
        }
    }
}