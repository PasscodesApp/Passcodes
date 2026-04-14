package com.jeeldobariya.passcodes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.core.domain.usecases.CheckForUpdateUseCase
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import com.jeeldobariya.passcodes.navigation.NavigationRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val checkForUpdateUseCase: CheckForUpdateUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            checkForUpdateUseCase(
                currentVersion = BuildConfig.VERSION_NAME,
                githubReleaseApiUrl = Constant.GITHUB_RELEASE_API_URL,
                telegramCommunityUrl = Constant.TELEGRAM_COMMUNITY_URL
            )
        }

        enableEdgeToEdge()
        setContent {
            PasscodesTheme {
                val featureFlagState by featureFlagsDatastore.data.collectAsState(
                    FeatureFlagsSettings(isPreviewLayoutEnabled = true)
                )

                if (!featureFlagState.isPreviewLayoutEnabled) {
                    val context = LocalContext.current

                    Intent(context, com.jeeldobariya.passcodes.oldui.MainActivity::class.java).also {
                        context.startActivity(it)
                    }
                }

                NavigationRoot()
            }
        }
    }
}
