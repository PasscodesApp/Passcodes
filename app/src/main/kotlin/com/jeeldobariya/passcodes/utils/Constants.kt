package com.jeeldobariya.passcodes.utils

object Constant {
    const val REPOSITORY_SIGNATURE = "PasscodesApp/Passcodes"
    const val DOCS_REPOSITORY_SIGNATURE = "PasscodesApp/Passcodes-Docs"

    // Main URL Constants
    const val TELEGRAM_COMMUNITY_URL = "https://t.me/passcodescommunity"
    const val REPOSITORY_URL = "https://github.com/$REPOSITORY_SIGNATURE"
    const val DOCS_REPOSITORY_URL = "https://github.com/$DOCS_REPOSITORY_SIGNATURE"

    // URL Constants
    const val GITHUB_RELEASE_API_URL = "https://api.github.com/repos/$REPOSITORY_SIGNATURE/releases"
    const val REPORT_BUG_URL = "$REPOSITORY_URL/issues/new?template=bug-report.md"
    const val RELEASE_NOTE_URL = "$DOCS_REPOSITORY_URL/blob/main/user-docs/release-notes.md"
    const val SECURITY_GUIDE_URL = "$DOCS_REPOSITORY_URL/blob/main/user-docs/security-guidelines.md"

    // Other App Constants
    const val IMPORT_EXPORT_CSV_HEADER = "name,url,username,password,note"
}
