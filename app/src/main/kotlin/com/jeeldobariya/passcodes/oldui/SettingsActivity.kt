package com.jeeldobariya.passcodes.oldui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivitySettingsBinding
import com.jeeldobariya.passcodes.flags.FeatureFlagManager
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Constant
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.launch

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
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInitialLangSelection()

        binding.switchLatestFeatures.isChecked = FeatureFlagManager.get(this).latestFeaturesEnabled

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
        Toast.makeText(
            this@SettingsActivity,
            getString(R.string.something_went_wrong_msg),
            Toast.LENGTH_SHORT
        ).show()
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
            val sharedPrefs = getSharedPreferences(Constant.APP_PREFS_NAME, MODE_PRIVATE)
            val currentThemeStyle =
                sharedPrefs.getInt(Constant.THEME_KEY, R.style.PasscodesTheme_Default)

            val currentIndex = THEMES.indexOf(currentThemeStyle)
            val nextIndex = (currentIndex + 1) % THEMES.size
            val newThemeStyle = THEMES[nextIndex]

            // Save the new theme and restart the application to apply it
            sharedPrefs.edit { putInt(Constant.THEME_KEY, newThemeStyle) }
            recreate()

            Toast.makeText(
                this@SettingsActivity,
                getString(R.string.restart_app_require),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchLatestFeatures.setOnCheckedChangeListener { _, isChecked ->
            FeatureFlagManager.get(this).latestFeaturesEnabled = isChecked
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
