package com.thekami.kamitv.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
)

object UpdateChecker {
    // TODO: change to your actual GitHub repo
    private const val REPO = "thekami-dev/kamiTV"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"

    suspend fun check(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(URL(API).readText())
            val latest = json.getString("tag_name").trimStart('v')
            if (!isNewer(latest, currentVersion)) return@withContext null
            val apkUrl = json.getJSONArray("assets")
                .let { arr -> (0 until arr.length()).map { arr.getJSONObject(it) } }
                .firstOrNull { it.getString("name").endsWith(".apk") }
                ?.getString("browser_download_url") ?: return@withContext null
            UpdateInfo(
                latestVersion = latest,
                downloadUrl = apkUrl,
                releaseNotes = json.optString("body", "").take(300),
            )
        } catch (_: Exception) { null }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val l = latest.split(".").mapNotNull { it.toIntOrNull() }
        val c = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
    }
}
