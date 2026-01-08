package app.pandorapass.pandora.logic.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.Locale

object LeakChecker {

    private val client = OkHttpClient()

    /**
     * @return the amount of leaks that the password was found in.
     */
    suspend fun checkPassword(password: String): Int = withContext(Dispatchers.IO) {
        val fullHash = sha1(password)
        val prefix = fullHash.take(5)
        val suffix = fullHash.substring(5).uppercase(Locale.ROOT)

        val request = Request.Builder()
            .url("https://api.pwnedpasswords.com/range/$prefix")
            .header("User-Agent", "Pandora-LeakChecker")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext -1
            val responseText = response.body?.string() ?: ""

            for (line in responseText.lines()) {
                if (line.startsWith(suffix)) {
                    return@withContext line.split(":")[1].trim().toInt()
                }
            }
        }
        return@withContext 0
    }

    private fun sha1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}