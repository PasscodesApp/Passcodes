package com.jeeldobariya.passcodes.utils

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable

val Context.appDatastore by dataStore(fileName = "app-settings-v1.json", serializer = AppSettingsSerializer)

@Serializable
data class AppSettings(
    val theme: String
)
