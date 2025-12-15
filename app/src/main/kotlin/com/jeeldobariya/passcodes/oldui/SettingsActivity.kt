package com.jeeldobariya.passcodes.oldui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.autofill.AutofillSettingsActivity
import com.jeeldobariya.passcodes.core.R
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.core.utils.collectLatestLifecycleFlow
import com.jeeldobariya.passcodes.databinding.ActivitySettingsBinding
import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {

    private val passwordRepository: PasswordRepository by inject()

    private lateinit var binding: ActivitySettingsBinding

    // List of available themes to cycle through
    @Suppress("PrivatePropertyName")
    private val THEMES = listOf(
        R.style.PasscodesTheme_Default,
        R.style.PasscodesTheme_Trusted,
        R.style.PasscodesTheme_Pink,
        R.style.PasscodesTheme_Cute,
        R.style.PasscodesTheme_GreenSafe
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInitialLangSelection()

        collectLatestLifecycleFlow(featureFlagsDatastore.data) {
            binding.switchLatestFeatures.isChecked = it.isPreviewFeaturesEnabled
            binding.switchLatestLayout.isChecked = it.isPreviewLayoutEnabled

            binding.autofillSettingCard.visibility =
                if (it.isPreviewFeaturesEnabled) View.VISIBLE else View.GONE
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // TODO: Shift the language switching logic to viewmodel in core module.
    private fun setInitialLangSelection() {
        val currentAppLocales: String = AppCompatDelegate.getApplicationLocales().toLanguageTags()

        val languageTags = resources.getStringArray(R.array.lang_locale_tags)
        for ((index, localeTag) in languageTags.withIndex()) {
            if (currentAppLocales.contains(localeTag)) {
                binding.langSwitchDropdown.setSelection(index)
                return
            }
        }

        binding.langSwitchDropdown.setSelection(0)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.langSwitchDropdown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val languageTags = resources.getStringArray(R.array.lang_locale_tags)
                    val localeTag = languageTags[position]
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeTag)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Not needed in this case, as we've already set a default
                }
            }

        binding.toggleThemeBtn.setOnClickListener {
            lifecycleScope.launch {
                // TODO: Shift the theme switching logic to viewmodel in core module.
                // INFO: logic here will change with jetpack compose.
                val currentThemeStyle = appDatastore.data.first().theme

                val currentIndex = THEMES.indexOf(currentThemeStyle)
                val nextIndex = (currentIndex + 1) % THEMES.size
                val newThemeStyle = THEMES[nextIndex]

                appDatastore.updateData { it.copy(theme = newThemeStyle) }

                finishAndRemoveTask()
                exitProcess(0)
            }
        }

        binding.switchLatestFeatures.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                featureFlagsDatastore.updateData {
                    it.copy(isPreviewFeaturesEnabled = isChecked)
                }
            }
        }

        binding.switchLatestLayout.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                featureFlagsDatastore.updateData {
                    it.copy(isPreviewLayoutEnabled = isChecked)
                }

                finishAndRemoveTask()
                exitProcess(0)
            }
        }

        binding.autofillSettingBtn.setOnClickListener { v ->
            val autofillSettingsIntent = Intent(this, AutofillSettingsActivity::class.java)
            startActivity(autofillSettingsIntent)
        }

        binding.clearAllDataBtn.setOnClickListener { v ->
            lifecycleScope.launch {
                passwordRepository.clearAllData()
            }

            Toast.makeText(this@SettingsActivity, "Delete the user data!!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
