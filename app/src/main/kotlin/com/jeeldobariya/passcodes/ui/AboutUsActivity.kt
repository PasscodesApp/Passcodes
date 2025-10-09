package com.jeeldobariya.passcodes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.jeeldobariya.passcodes.databinding.ActivityAboutUsBinding
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Constant

class AboutUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up button click listeners
        setupButtonListeners()
    }

    private fun openBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    }

    private fun setupButtonListeners() {
        binding.cardSecurityGuidelines.setOnClickListener {
            openBrowser(Constant.SECURITY_GUIDE_URL)
        }

        binding.cardReleaseNotes.setOnClickListener {
            openBrowser(Constant.RELEASE_NOTE_URL)
        }

        binding.cardLicense.setOnClickListener {
            startActivity(Intent(this, LicenseActivity::class.java))
        }

        binding.cardReportBug.setOnClickListener {
            openBrowser(Constant.REPORT_BUG_URL)
        }

        binding.cardTelegramCommunity.setOnClickListener {
            openBrowser(Constant.TELEGRAM_COMMUNITY_URL)
        }
    }
}