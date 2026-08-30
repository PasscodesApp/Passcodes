package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.autofill.AutofillManager

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class AutofillSettingsActivity : AppCompatActivity() {

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

        updateAutofillStatus()
        
        WindowCompat.enableEdgeToEdge(window)
    }

    override fun onResume() {
        super.onResume()
        updateAutofillStatus()
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
