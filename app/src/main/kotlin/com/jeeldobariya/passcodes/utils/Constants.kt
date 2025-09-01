package com.jeeldobariya.passcodes.utils

object Constant {
    const val REPOSITORY_SIGNATURE = "PasscodesApp/Passcodes"

    // URL Constants
    const val TELEGRAM_COMMUNITY_URL = "https://t.me/passcodescommunity"
    const val REPOSITORY_URL = "https://github.com/$REPOSITORY_SIGNATURE"
    const val GITHUB_RELEASE_API_URL = "https://api.github.com/repos/$REPOSITORY_SIGNATURE/releases"
    const val REPORT_BUG_URL = "$REPOSITORY_URL/issues/new?template=bug-report.md"
    const val RELEASE_NOTE_URL = "$REPOSITORY_URL/blob/main/docs/release-notes.md"
    const val SECURITY_GUIDE_URL = "$REPOSITORY_URL/blob/main/docs/security-guide.md"


    // Shared Preferences Constants
    const val APP_PREFS_NAME = "app_prefs"
    const val THEME_KEY = "selected_theme"

    const val FEATURE_FLAGS_PREFS_NAME = "feature_flags_prefs"
    const val LATEST_FEATURES_KEY = "latest_features_enabled"
}
