package com.jeeldobariya.passcodes.flags

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object FeatureFlagsSettingsSerializer: Serializer<FeatureFlagsSettings> {
    override val defaultValue: FeatureFlagsSettings
        get() = FeatureFlagsSettings()

    override suspend fun readFrom(input: InputStream): FeatureFlagsSettings {
        return try {
            Json.decodeFromString(
                deserializer = FeatureFlagsSettings.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: FeatureFlagsSettings,
        output: OutputStream
    ) {
        output.write(
            Json.encodeToString(
                serializer = FeatureFlagsSettings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}
