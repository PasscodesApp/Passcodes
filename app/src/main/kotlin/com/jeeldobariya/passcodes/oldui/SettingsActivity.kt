package com.jeeldobariya.passcodes.oldui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivitySettingsBinding
import com.jeeldobariya.passcodes.flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.utils.Controller
import com.jeeldobariya.passcodes.utils.appDatastore
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var controller: Controller

    // List of available themes to cycle through
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
        }

        controller = Controller(this) // Initialize the controller here

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

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
                val currentThemeStyle = appDatastore.data.first().theme

                val currentIndex = THEMES.indexOf(currentThemeStyle)
                val nextIndex = (currentIndex + 1) % THEMES.size
                val newThemeStyle = THEMES[nextIndex]

                appDatastore.updateData { it.copy(theme = newThemeStyle) }

                recreate()
            }

            Toast.makeText(
                this@SettingsActivity,
                getString(R.string.restart_app_require),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchLatestFeatures.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                featureFlagsDatastore.updateData {
                    it.copy(isPreviewFeaturesEnabled = isChecked)
                }
            }
            Toast.makeText(
                this@SettingsActivity,
                getString(R.string.future_feat_clause) + isChecked.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.clearAllDataBtn.setOnClickListener { v ->
            lifecycleScope.launch {
                controller.clearAllData()
            }

            Toast.makeText(this@SettingsActivity, "Delete the user data!!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
