package com.jeeldobariya.passcodes

object Constant {

    const val GH_DOMAIN_URL = "https://passcodesapp.github.io"
    const val REPOSITORY_SIGNATURE = "PasscodesApp/Passcodes"

    // Main URL Constants
    const val WEBSITE_URL = "$GH_DOMAIN_URL/Passcodes-Website"
    const val DOCS_WEBSITE_URL = "$GH_DOMAIN_URL/Passcodes-Docs"
    const val DISCORD_COMMUNITY_URL = "https://discord.gg/zPbk2gp3Sg"
    const val REPOSITORY_URL = "https://github.com/$REPOSITORY_SIGNATURE"

    // URL Constants
    const val GITHUB_RELEASE_API_URL = "https://api.github.com/repos/$REPOSITORY_SIGNATURE/releases"
    const val REPORT_BUG_URL = "$REPOSITORY_URL/issues/new?template=bug-report.md"
    const val RELEASE_NOTE_URL = "$DOCS_WEBSITE_URL/user-docs/release-notes/"
    const val SECURITY_GUIDE_URL = "$DOCS_WEBSITE_URL/user-docs/security-guidelines/"
}
