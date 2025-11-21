package com.jeeldobariya.passcodes.utils

// please refer to `app/src/main/kotlin/com/jeeldobariya/passcodes/flags/FeatureFlagsSettings.kt` for Migration Guide.

import android.content.Context
import androidx.datastore.dataStore
import com.jeeldobariya.passcodes.R
import kotlinx.serialization.Serializable

val Context.appDatastore by dataStore(fileName = "app-settings.json", serializer = AppSettingsSerializer)

@Serializable
data class AppSettings(
    val theme: Int = R.style.PasscodesTheme_Default,
)
