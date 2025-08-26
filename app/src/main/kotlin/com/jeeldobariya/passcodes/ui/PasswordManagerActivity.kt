package com.jeeldobariya.passcodes.ui

import android.content.Intent
import android.view.View.GONE
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivityPasswordManagerBinding
import com.jeeldobariya.passcodes.flags.FeatureFlagManager
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PasswordManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordManagerBinding
    private lateinit var controller: Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!FeatureFlagManager.get(this).latestFeaturesEnabled) {
            binding.importPasswordBtn.visibility = GONE
            binding.exportPasswordBtn.visibility = GONE
        }

        controller = Controller(this) // Initialize the controller here

        // Add event onclick listener
        addOnClickListenerOnButton(binding)

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton(binding: ActivityPasswordManagerBinding) {
        binding.savePasswordBtn.setOnClickListener {
            val savePasswordIntent = Intent(this, SavePasswordActivity::class.java)
            startActivity(savePasswordIntent)
        }

        binding.loadPasswordBtn.setOnClickListener {
            val loadPasswordIntent = Intent(this, LoadPasswordActivity::class.java)
            startActivity(loadPasswordIntent)
        }

        binding.importPasswordBtn.setOnClickListener {
            Toast.makeText(this, getString(R.string.future_feat_clause), Toast.LENGTH_SHORT).show()
        }

        binding.exportPasswordBtn.setOnClickListener {
            Toast.makeText(this, getString(R.string.future_feat_clause), Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(context = Dispatchers.IO) {
                val dataExportString = controller.generateCsvDataExportString()
                Log.i("DataExport", dataExportString)
            }
        }
    }
}