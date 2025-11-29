package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AutofillSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autofill_settings)

        val enableAutofillButton = findViewById<MaterialButton>(R.id.enable_autofill_button)
        enableAutofillButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
}
