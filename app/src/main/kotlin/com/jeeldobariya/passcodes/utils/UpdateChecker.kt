package com.jeeldobariya.passcodes.utils

import android.content.Context
import android.widget.Toast
import okhttp3.*
import java.io.IOException

object UpdateChecker {
    private const val RELEASES_URL =
        "https://api.github.com/repos/JeelDobariya38/Passcodes/releases"

    private val client = OkHttpClient()

    fun checkVersion(context: Context, currentVersion: String) {
        val request = Request.Builder()
            .url(RELEASES_URL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val releases = SemVerUtils.parseReleases(body)

                var userReleaseFound = false
                var latestStable: String? = null

                for (release in releases) {
                    if (release.draft) continue // ignore drafts

                    if (release.tag == currentVersion) {
                        userReleaseFound = true
                        if (release.prerelease) {
                            showToast(
                                context,
                                "⚠️ You are using a PRE-RELEASE build ($currentVersion). Not safe for production!"
                            )
                        }
                    }

                    if (!release.prerelease) {
                        if (latestStable == null ||
                            SemVerUtils.compare(release.tag, latestStable) > 0
                        ) {
                            latestStable = release.tag
                        }
                    }
                }

                latestStable?.let {
                    if (SemVerUtils.compare(currentVersion, it) < 0) {
                        showToast(context, "Update available: $it")
                    }
                }

                if (!userReleaseFound) {
                    showToast(context, "⚠️ Version ($currentVersion) not found on GitHub releases")
                }
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
