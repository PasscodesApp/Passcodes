package com.jeeldobariya.passcodes.utils

import android.content.Context
import android.widget.Toast
import okhttp3.*
import java.io.IOException

object UpdateChecker {
    private val client = OkHttpClient()

    fun checkVersion(context: Context, currentVersion: String) {
        context = context.applicationContext
        currentVersion = SemVerUtils.normalize(currentVersion)

        val request = Request.Builder()
            .url(Constant.GITHUB_RELEASE_API_URL)
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
                                "⚠️ You are using a PRE-RELEASE ($currentVersion). Not safe for use!  Join telegram community (${Constant.TELEGRAM_COMMUNITY_URL})"
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
                        showToast(context, "New Update available: $it... Vist our website...")
                    }
                }

                if (!userReleaseFound) {
                    showToast(context, "⚠️ Version ($currentVersion) not found on GitHub releases... Join telegram community (${Constant.TELEGRAM_COMMUNITY_URL})")
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
