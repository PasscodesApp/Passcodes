package com.jeeldobariya.passcodes.core.domain.usecases

import android.content.Context
import android.widget.Toast
import com.jeeldobariya.passcodes.core.domain.utils.SemVerUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CheckForUpdateUseCase(
    private val context: Context,
    private val client: HttpClient,
    private val dispatcher: CoroutineContext = Dispatchers.IO
) {
    suspend operator fun invoke(
        currentVersion: String,
        githubReleaseApiUrl: String,
        telegramCommunityUrl: String
    ) = withContext(dispatcher) {
        val currNormalizedVersion = SemVerUtils.normalize(currentVersion)

        try {
            val response = client.get(githubReleaseApiUrl)
            val body = response.bodyAsText()
            val releases = SemVerUtils.parseReleases(body)

            var userReleaseFound = false
            var latestStable: String? = null

            for (release in releases) {
                if (release.draft) continue

                if (release.tag == currNormalizedVersion) {
                    userReleaseFound = true
                    if (release.prerelease) {
                        showToast("⚠️ You are using a PRE-RELEASE ($currNormalizedVersion). Not safe for use!!")
                        showToast("Join telegram @ ($telegramCommunityUrl)")
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
                    showToast("New Update available: $it... Visit our website...")
                }
            }

            if (!userReleaseFound) {
                showToast("⚠️ Version ($currNormalizedVersion) not found on GitHub releases...")
                showToast("Join telegram @ ($telegramCommunityUrl)")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun showToast(message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
