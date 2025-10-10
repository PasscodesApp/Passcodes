package com.jeeldobariya.passcodes.flags

import android.content.Context
import androidx.core.content.edit
import com.jeeldobariya.passcodes.utils.Constant

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
