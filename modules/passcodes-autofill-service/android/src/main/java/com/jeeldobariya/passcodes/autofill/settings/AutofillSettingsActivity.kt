package com.jeeldobariya.passcodes.autofill.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.autofill.AutofillManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

import com.jeeldobariya.passcodes.autofill.R
import com.jeeldobariya.passcodes.autofill.unlock.AutofillUnlockActivity

class AutofillSettingsActivity : AppCompatActivity() {

    private lateinit var authenticationSwitch: MaterialSwitch
    private lateinit var inlineSwitch: MaterialSwitch
    private lateinit var fillModeGroup: MaterialButtonToggleGroup
    private lateinit var fillModeDescription: TextView

    // Guards the brief window where we're programmatically setting a
    // control's initial state — without this, that initial set call would
    // itself fire the listener, re-persist the value it was just loaded
    // from, and (for authentication) pop the recommendation dialog.
    private var isLoadingPreferences = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_autofill_settings)

        findViewById<MaterialButton>(R.id.autofill_enable_button)
            .setOnClickListener { openSystemAutofillSettings() }

        findViewById<MaterialToolbar>(R.id.autofill_toolbar)
            .setNavigationOnClickListener { finish() }

        authenticationSwitch = findViewById(R.id.autofill_authentication_switch)
        inlineSwitch = findViewById(R.id.autofill_inline_switch)
        fillModeGroup = findViewById(R.id.autofill_fill_mode_group)
        fillModeDescription = findViewById(R.id.autofill_fill_mode_description)

        setUpPreferenceControls()

        updateAutofillStatus()

        WindowCompat.enableEdgeToEdge(window)
    }

    override fun onResume() {
        super.onResume()
        updateAutofillStatus()
        updateAuthenticationHint()
    }

    private fun setUpPreferenceControls() {
        // Every control starts disabled (see the layout) until its real
        // stored value has loaded, so the user never sees — or taps —
        // a value we haven't confirmed yet.
        Thread {
            val biometricEnabled = AutofillPreferences.isBiometricLockEnabledBlocking(applicationContext)
            val inlineEnabled = AutofillPreferences.isInlineSuggestionsEnabledBlocking(applicationContext)
            val fillMode = AutofillPreferences.getFillModeBlocking(applicationContext)

            runOnUiThread {
                isLoadingPreferences = true
                authenticationSwitch.isChecked = biometricEnabled
                inlineSwitch.isChecked = inlineEnabled
                fillModeGroup.check(buttonIdFor(fillMode))
                showFillModeDescription(fillMode)

                authenticationSwitch.isEnabled = true
                inlineSwitch.isEnabled = true
                for (i in 0 until fillModeGroup.childCount) fillModeGroup.getChildAt(i).isEnabled = true
                isLoadingPreferences = false
            }
        }.start()

        // Tapping anywhere on a row toggles its switch, not just the thumb.
        findViewById<View>(R.id.autofill_authentication_setting).setOnClickListener {
            if (authenticationSwitch.isEnabled) authenticationSwitch.toggle()
        }
        findViewById<View>(R.id.autofill_inline_setting).setOnClickListener {
            if (inlineSwitch.isEnabled) inlineSwitch.toggle()
        }

        authenticationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            Thread { AutofillPreferences.setBiometricLockEnabledBlocking(applicationContext, isChecked) }.start()

            // The lock is the user's call and is already saved. If inline
            // suggestions are still on, we just *recommend* turning them
            // off — see showInlineRecommendation.
            if (isChecked && inlineSwitch.isChecked) showInlineRecommendation()
        }

        inlineSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            Thread { AutofillPreferences.setInlineSuggestionsEnabledBlocking(applicationContext, isChecked) }.start()
        }

        fillModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked || isLoadingPreferences) return@addOnButtonCheckedListener
            val mode = fillModeFor(checkedId)
            showFillModeDescription(mode)
            Thread { AutofillPreferences.setFillModeBlocking(applicationContext, mode) }.start()
        }
    }

    /**
     * Inline chips for locked entries can only say "Tap to unlock" (the
     * username is hidden until authentication), so they're less useful
     * than the dropdown. We recommend turning inline off, but leave the
     * decision to the user — either answer keeps authentication on.
     */
    private fun showInlineRecommendation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.autofill_recommend_title)
            .setMessage(R.string.autofill_recommend_message)
            .setPositiveButton(R.string.autofill_recommend_turn_off) { _, _ ->
                // The listener persists the new value.
                inlineSwitch.isChecked = false
            }
            .setNegativeButton(R.string.autofill_recommend_keep, null)
            .show()
    }

    private fun buttonIdFor(mode: FillMode): Int = when (mode) {
        FillMode.SMART -> R.id.autofill_mode_smart
        FillMode.AUTO -> R.id.autofill_mode_auto
        FillMode.DUMB -> R.id.autofill_mode_dumb
    }

    private fun fillModeFor(buttonId: Int): FillMode = when (buttonId) {
        R.id.autofill_mode_smart -> FillMode.SMART
        R.id.autofill_mode_dumb -> FillMode.DUMB
        else -> FillMode.AUTO
    }

    private fun showFillModeDescription(mode: FillMode) {
        fillModeDescription.setText(
            when (mode) {
                FillMode.SMART -> R.string.autofill_fill_mode_smart_description
                FillMode.AUTO -> R.string.autofill_fill_mode_auto_description
                FillMode.DUMB -> R.string.autofill_fill_mode_dumb_description
            }
        )
    }

    /** Warns when the lock switch can't actually do anything on this device. */
    private fun updateAuthenticationHint() {
        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate(AutofillUnlockActivity.ALLOWED_AUTHENTICATORS)
        findViewById<View>(R.id.autofill_authentication_hint).visibility =
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) View.GONE else View.VISIBLE
    }

    private fun updateAutofillStatus() {
        val statusText = findViewById<TextView>(R.id.autofill_status)
        val statusIcon = findViewById<ImageView>(R.id.autofill_status_icon)
        val enableButton = findViewById<MaterialButton>(R.id.autofill_enable_button)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            statusText.setText(R.string.autofill_status_unsupported)
            statusIcon.setImageResource(R.drawable.ic_error_outline)
            enableButton.visibility = View.GONE
            return
        }

        val manager = getSystemService(AutofillManager::class.java)
        val enabled = manager?.hasEnabledAutofillServices() == true

        statusText.setText(
            if (enabled) R.string.autofill_status_enabled else R.string.autofill_status_disabled
        )
        statusIcon.setImageResource(
            if (enabled) R.drawable.ic_check_circle else R.drawable.ic_error_outline
        )
        enableButton.setText(
            if (enabled) R.string.autofill_button_change else R.string.autofill_button_enable
        )
    }

    private fun openSystemAutofillSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val intent = Intent(
            Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
        ).apply {
            data = Uri.parse("package:$packageName")
        }

        startActivity(intent)
    }
}
