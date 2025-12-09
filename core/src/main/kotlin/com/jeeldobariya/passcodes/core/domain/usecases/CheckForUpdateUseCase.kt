package com.jeeldobariya.passcodes.core.domain.usecases

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.jeeldobariya.passcodes.core.domain.utils.SemVerUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class CheckForUpdateUseCase(
    val context: Context,
    val client: OkHttpClient
) {
     suspend operator fun invoke(currentVersion: String, githubReleaseApiUrl: String, telegramCommunityUrl: String) {
        val currNormalizedVersion = SemVerUtils.normalize(currentVersion)

        val request = Request.Builder()
            .url(githubReleaseApiUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body.string()
                val releases = SemVerUtils.parseReleases(body)

                var userReleaseFound = false
                var latestStable: String? = null

                for (release in releases) {
                    if (release.draft) continue // ignore drafts

                    if (release.tag == currNormalizedVersion) {
                        userReleaseFound = true
                        if (release.prerelease) {
                            showToast(
                                context,
                                "⚠️ You are using a PRE-RELEASE ($currNormalizedVersion). Not safe for use!  Join telegram community ($telegramCommunityUrl)"
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
                    if (SemVerUtils.compare(currNormalizedVersion, it) < 0) {
                        showToast(context, "New Update available: $it... Vist our website...")
                    }
                }

                if (!userReleaseFound) {
                    showToast(
                        context,
                        "⚠️ Version ($currNormalizedVersion) not found on GitHub releases... Join telegram community ($telegramCommunityUrl)"
                    )
                }
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
