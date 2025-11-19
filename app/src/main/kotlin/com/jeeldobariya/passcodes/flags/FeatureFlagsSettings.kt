package com.jeeldobariya.passcodes.flags

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlagsSettings(
    val isPreviewFeaturesEnabled: Boolean,
    val isPreviewLayoutEnabled: Boolean
)
