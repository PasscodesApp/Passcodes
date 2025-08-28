package com.jeeldobariya.passcodes.utils

import org.json.JSONArray

object SemVerUtils {
    /** Compare two semantic versions.
     * > 0 if v1 > v2, < 0 if v1 < v2, 0 if equal
     */
    fun compare(v1: String, v2: String): Int {
        val cleanV1 = v1.trimStart('v', 'V')
        val cleanV2 = v2.trimStart('v', 'V')

        val parts1 = cleanV1.split(".")
        val parts2 = cleanV2.split(".")

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrNull(i)?.toIntOrNull() ?: 0
            val p2 = parts2.getOrNull(i)?.toIntOrNull() ?: 0
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    data class Release(
        val tag: String,
        val prerelease: Boolean,
        val draft: Boolean
    )

    fun parseReleases(json: String): List<Release> {
        val array = JSONArray(json)
        val list = mutableListOf<Release>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Release(
                    tag = obj.getString("tag_name"),
                    prerelease = obj.getBoolean("prerelease"),
                    draft = obj.getBoolean("draft")
                )
            )
        }
        return list
    }
}
