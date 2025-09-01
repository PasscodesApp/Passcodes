package com.jeeldobariya.passcodes.utils

import org.junit.Test
import com.google.common.truth.Truth.assertThat

class SemVerUtilsTest {

    @Test
    fun testCompareEqual() {
        assertThat(SemVerUtils.compare("1.2.3", "1.2.3")).isEqualTo(0)
        assertThat(SemVerUtils.compare("v1.0.0", "v1.0.0")).isEqualTo(0)
    }

    @Test
    fun testCompareGreater() {
        assertThat(SemVerUtils.compare("1.2.10", "1.2.2")).isGreaterThan(0)
        assertThat(SemVerUtils.compare("v2.0.0", "v1.9.9")).isGreaterThan(0)
    }

    @Test
    fun testCompareLess() {
        assertThat(SemVerUtils.compare("1.2.2", "v1.2.10")).isLessThan(0)
        assertThat(SemVerUtils.compare("v0.9.0", "1.0.0")).isLessThan(0)
    }

    @Test
    fun testPrefixV() {
        assertThat(SemVerUtils.compare("v1.2.10", "v1.2.2")).isGreaterThan(0)
        assertThat(SemVerUtils.compare("V2.0.0", "V1.9.9")).isGreaterThan(0)
        assertThat(SemVerUtils.compare("1.2.2", "v1.2.10")).isLessThan(0)
        assertThat(SemVerUtils.compare("v0.9.0", "1.0.0")).isLessThan(0)
    }

    @Test
    fun testNormalize() {
        assertThat(SemVerUtils.normalize("v1.2.3-beta")).isEqualTo("v1.2.3")
        assertThat(SemVerUtils.normalize("1.0.0-alpha.1+001")).isEqualTo("v1.0.0")
        assertThat(SemVerUtils.normalize("V2.3.4-rc1")).isEqualTo("v2.3.4")
        assertThat(SemVerUtils.normalize("1.5.0")).isEqualTo("v1.5.0")
        assertThat(SemVerUtils.normalize("v2.5.0")).isEqualTo("v2.5.0")
        assertThat(SemVerUtils.normalize("v1.0.0-Stable-Dev")).isEqualTo("v1.0.0")

        // Note: try some invalid / wired stuff just to test
        assertThat(SemVerUtils.normalize("v1.0-Stable-Dev")).isEqualTo("v1.0")
        assertThat(SemVerUtils.normalize("v1.0------")).isEqualTo("v1.0")
        assertThat(SemVerUtils.normalize("v1.0-abc")).isEqualTo("v1.0")
        assertThat(SemVerUtils.normalize("v--1.0-abc")).isEqualTo("v")
        assertThat(SemVerUtils.normalize("")).isEqualTo("v")
    }

    @Test
    fun testParseReleases() {
        val json = """
            [
                {
                    "url": "https://api.github.com/repos/JeelDobariya38/Passcodes/releases/240385777",
                    "assets_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/releases/240385777/assets",
                    "upload_url": "https://uploads.github.com/repos/JeelDobariya38/Passcodes/releases/240385777/assets{?name,label}",
                    "html_url": "https://github.com/JeelDobariya38/Passcodes/releases/tag/v1.0.0",
                    "id": 240385777,
                    "author": {},
                    "node_id": "RE_kwDOMffp084OU_7x",
                    "tag_name": "v1.0.0",
                    "target_commitish": "main",
                    "name": "v1.0.0 - Stable Release",
                    "draft": false,
                    "immutable": false,
                    "prerelease": false,
                    "created_at": "2025-08-16T17:23:16Z",
                    "updated_at": "2025-08-17T12:56:19Z",
                    "published_at": "2025-08-16T18:29:16Z",
                    "assets": [],
                    "tarball_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/tarball/v1.0.0",
                    "zipball_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/zipball/v1.0.0",
                    "body": "",
                    "mentions_count": 3
                },
                {
                    "url": "https://api.github.com/repos/JeelDobariya38/Passcodes/releases/171838408",
                    "assets_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/releases/171838408/assets",
                    "upload_url": "https://uploads.github.com/repos/JeelDobariya38/Passcodes/releases/171838408/assets{?name,label}",
                    "html_url": "https://github.com/JeelDobariya38/Passcodes/releases/tag/v0.1.0",
                    "id": 171838408,
                    "author": {},
                    "node_id": "RE_kwDOMffp084KPgvI",
                    "tag_name": "v0.1.0",
                    "target_commitish": "main",
                    "name": "v0.1.0 - Alpha Release [Yanked Released]",
                    "draft": false,
                    "immutable": false,
                    "prerelease": true,
                    "created_at": "2024-08-25T16:13:32Z",
                    "updated_at": "2025-08-16T16:09:32Z",
                    "published_at": "2024-08-26T03:51:02Z",
                    "assets": [],
                    "tarball_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/tarball/v0.1.0",
                    "zipball_url": "https://api.github.com/repos/JeelDobariya38/Passcodes/zipball/v0.1.0",
                    "body": "",
                    "mentions_count": 2
                }
            ]
        """.trimIndent()

        val releases = SemVerUtils.parseReleases(json)
        
        assertThat(releases.size).isEqualTo(2)
        assertThat(releases[0].tag).isEqualTo("v1.0.0")
        assertThat(releases[0].prerelease).isFalse()
        assertThat(releases[1].prerelease).isTrue()
    }
}
