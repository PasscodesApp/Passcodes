package com.jeeldobariya.passcodes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import com.jeeldobariya.passcodes.navigation.NavigationRoot
import com.jeeldobariya.passcodes.oldui.MainActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasscodesTheme {
                val featureFlagState by featureFlagsDatastore.data.collectAsState(
                    FeatureFlagsSettings(isPreviewLayoutEnabled = true)
                )

                if (!featureFlagState.isPreviewLayoutEnabled) {
                    val content = LocalContext.current

                    Intent(content, MainActivity::class.java).also {
                        content.startActivity(it)
                    }
                }

                NavigationRoot()
            }
        }
    }
}