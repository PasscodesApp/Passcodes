package com.jeeldobariya.passcodes.password_manager.oldui

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.core.utils.collectLatestLifecycleFlow
import com.jeeldobariya.passcodes.password_manager.R
import com.jeeldobariya.passcodes.password_manager.databinding.ActivityPasswordManagerBinding
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ImportPasswordCSVUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class PasswordManagerActivity : AppCompatActivity() {
    private val importPasswordUseCase: ImportPasswordCSVUseCase by inject()

    private val exportPasswordUseCase: ExportPasswordCSVUseCase by inject()

    private lateinit var binding: ActivityPasswordManagerBinding

    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            requireNotNull(uri)

            Toast.makeText(this@PasswordManagerActivity, "Exporting...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                exportPasswordUseCase(uri)
            }
        } else {
            Toast.makeText(this@PasswordManagerActivity, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    private val importCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            requireNotNull(uri)

            Toast.makeText(this@PasswordManagerActivity, "Importing...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                importPasswordUseCase(uri)
            }
        } else {
            Toast.makeText(this@PasswordManagerActivity, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        collectLatestLifecycleFlow(featureFlagsDatastore.data) {
            if (!it.isPreviewFeaturesEnabled) {
                binding.importPasswordBtn.visibility = GONE
                binding.exportPasswordBtn.visibility = GONE
            }
        }

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
            Toast.makeText(
                this@PasswordManagerActivity,
                getString(R.string.preview_feature),
                Toast.LENGTH_LONG
            ).show()

            importCsvFilePicker()
        }

        binding.exportPasswordBtn.setOnClickListener {
            Toast.makeText(
                this@PasswordManagerActivity,
                getString(R.string.preview_feature),
                Toast.LENGTH_LONG
            ).show()

            lifecycleScope.launch(Dispatchers.Main) {
                exportCsvFilePicker()
            }
        }
    }

    private fun exportCsvFilePicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType("text/comma-separated-values")
            putExtra(Intent.EXTRA_TITLE, "passwords.csv")
        }

        exportCsvLauncher.launch(intent)
    }

    private fun importCsvFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType("text/comma-separated-values")
            putExtra(Intent.EXTRA_TITLE, "passwords.csv")
        }

        importCsvLauncher.launch(intent)
    }
}
