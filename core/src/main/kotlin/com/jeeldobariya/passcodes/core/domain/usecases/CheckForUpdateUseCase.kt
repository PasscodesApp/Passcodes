package com.jeeldobariya.passcodes.core.domain.usecases

import android.content.Context
import android.widget.Toast
import com.jeeldobariya.passcodes.core.domain.utils.SemVerUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

enum class UpdateCheckingResult {
    ALREADY_ON_LATEST_RELEASE,
    UPDATE_AVAILABLE,
    ON_PRE_RELEASE,
    ON_UNOFFICIAL_RELEASE,
    ERROR
}

class CheckForUpdateUseCase(
    private val context: Context,
    private val client: HttpClient,
    private val dispatcher: CoroutineContext = Dispatchers.IO
) {
    suspend operator fun invoke(
        currentVersion: String,
        githubReleaseApiUrl: String,
        telegramCommunityUrl: String
    ): UpdateCheckingResult = withContext(dispatcher) {
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

                        return@withContext UpdateCheckingResult.ON_PRE_RELEASE
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
                    return@withContext UpdateCheckingResult.UPDATE_AVAILABLE
                }
            }

            if (!userReleaseFound) {
                showToast("⚠️ Version ($currNormalizedVersion) is not an official releases on github...")
                showToast("Join telegram @ ($telegramCommunityUrl)")

                return@withContext UpdateCheckingResult.ON_UNOFFICIAL_RELEASE
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext UpdateCheckingResult.ERROR
        }

        return@withContext UpdateCheckingResult.ALREADY_ON_LATEST_RELEASE
    }

    private suspend fun showToast(message: String) = withContext(Dispatchers.Main) {
        ensureActive()
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
