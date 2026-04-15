package com.jeeldobariya.passcodes

object Constant {
    const val REPOSITORY_SIGNATURE = "PasscodesApp/Passcodes"

    // Main URL Constants
    const val DOCS_WEBSITE_URL = "https://passcodesapp.github.io/Passcodes-Docs"
    const val TELEGRAM_COMMUNITY_URL = "https://t.me/passcodescommunity"
    const val REPOSITORY_URL = "https://github.com/$REPOSITORY_SIGNATURE"

    // URL Constants
    const val GITHUB_RELEASE_API_URL = "https://api.github.com/repos/$REPOSITORY_SIGNATURE/releases"
    const val REPORT_BUG_URL = "$REPOSITORY_URL/issues/new?template=bug-report.md"
    const val RELEASE_NOTE_URL = "$DOCS_WEBSITE_URL/user-docs/release-notes/"
    const val SECURITY_GUIDE_URL = "$DOCS_WEBSITE_URL/user-docs/security-guidelines/"
}
