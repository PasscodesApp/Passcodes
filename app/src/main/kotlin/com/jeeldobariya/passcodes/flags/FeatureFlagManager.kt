package com.jeeldobariya.passcodes.flags

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.first

val Context.featureFlagsDatastore by dataStore(fileName = "feature-flags-settings.json", serializer = FeatureFlagsSettingsSerializer)

suspend fun Context.isPreviewFeaturesEnable(): Boolean {
    return featureFlagsDatastore.data.first().isPreviewFeaturesEnabled
}

suspend fun Context.togglePreviewFeatures(): Boolean {
    return featureFlagsDatastore.updateData {
        it.copy(isPreviewFeaturesEnabled = !it.isPreviewFeaturesEnabled)
    }.isPreviewFeaturesEnabled
}

suspend fun Context.isPreviewLayoutEnable(): Boolean {
    return featureFlagsDatastore.data.first().isPreviewLayoutEnabled
}

suspend fun Context.togglePreviewLayout(): Boolean {
    return featureFlagsDatastore.updateData {
        it.copy(isPreviewLayoutEnabled = !it.isPreviewLayoutEnabled)
    }.isPreviewLayoutEnabled
}

/*
class FeatureFlagManager private constructor(context: Context) {

    private val prefs =
        context.getSharedPreferences(Constant.FEATURE_FLAGS_PREFS_NAME, Context.MODE_PRIVATE)

    var latestFeaturesEnabled: Boolean
        get() = prefs.getBoolean(Constant.LATEST_FEATURES_KEY, false)
        set(value) {
            prefs.edit { putBoolean(Constant.LATEST_FEATURES_KEY, value) }
        }

    companion object {
        @Volatile
        private var INSTANCE: FeatureFlagManager? = null

        fun get(context: Context): FeatureFlagManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FeatureFlagManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
*/
