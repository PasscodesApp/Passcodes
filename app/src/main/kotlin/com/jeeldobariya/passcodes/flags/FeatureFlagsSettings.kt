package com.jeeldobariya.passcodes.flags

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable

val Context.featureFlagsDatastore by dataStore(fileName = "feature-flags-settings-v1.json", serializer = FeatureFlagsSettingsSerializer)

@Serializable
data class FeatureFlagsSettings(
    val isPreviewFeaturesEnabled: Boolean,
    val isPreviewLayoutEnabled: Boolean
)
