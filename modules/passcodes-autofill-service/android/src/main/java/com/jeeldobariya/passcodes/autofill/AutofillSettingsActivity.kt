package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.autofill.AutofillManager

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.materialswitch.MaterialSwitch

class AutofillSettingsActivity : AppCompatActivity() {

    private lateinit var authenticationSwitch: MaterialSwitch
    private lateinit var inlineSwitch: MaterialSwitch

    // Guards the brief window where we're programmatically setting a
    // switch's initial state — without this, that initial setChecked()
    // call would itself fire the listener and re-persist the value it
    // was just loaded from, which is harmless but wasteful.
    private var isLoadingPreferences = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_autofill_settings)

        findViewById<android.widget.Button>(
            R.id.autofill_enable_button
        ).setOnClickListener {
            openSystemAutofillSettings()
        }

        findViewById<com.google.android.material.appbar.MaterialToolbar>(
            R.id.autofill_toolbar
        ).setNavigationOnClickListener {
            finish()
        }

        authenticationSwitch = findViewById(R.id.autofill_authentication_switch)
        inlineSwitch = findViewById(R.id.autofill_inline_switch)
        setUpPreferenceSwitches()

        updateAutofillStatus()

        WindowCompat.enableEdgeToEdge(window)
    }

    override fun onResume() {
        super.onResume()
        updateAutofillStatus()
    }

    private fun setUpPreferenceSwitches() {
        // Both switches start disabled (see the layout) until their real
        // stored value has loaded, so the user never sees — or taps —
        // a value we haven't confirmed yet.
        Thread {
            val biometricEnabled = AutofillPreferences.isBiometricLockEnabledBlocking(applicationContext)
            val inlineEnabled = AutofillPreferences.isInlineSuggestionsEnabledBlocking(applicationContext)

            runOnUiThread {
                isLoadingPreferences = true
                authenticationSwitch.isChecked = biometricEnabled
                inlineSwitch.isChecked = inlineEnabled
                authenticationSwitch.isEnabled = true
                inlineSwitch.isEnabled = true
                isLoadingPreferences = false
            }
        }.start()

        authenticationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            Thread { AutofillPreferences.setBiometricLockEnabledBlocking(applicationContext, isChecked) }.start()
        }

        inlineSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingPreferences) return@setOnCheckedChangeListener
            Thread { AutofillPreferences.setInlineSuggestionsEnabledBlocking(applicationContext, isChecked) }.start()
        }
    }

    private fun updateAutofillStatus() {
        val statusText =
            findViewById<android.widget.TextView>(
                R.id.autofill_status
            )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            statusText.text =
                "Autofill is not supported on this Android version."
            return
        }

        val manager =
            getSystemService(AutofillManager::class.java)

        val enabled =
            manager?.hasEnabledAutofillServices() == true

        statusText.text =
            if (enabled) {
                "Passcodes is your Autofill service."
            } else {
                "Passcodes is not your Autofill service."
            }
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
