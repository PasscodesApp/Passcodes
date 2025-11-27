package com.jeeldobariya.passcodes.oldui

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivityPasswordManagerBinding
import com.jeeldobariya.passcodes.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.domain.usecases.ImportPasswordCSVUseCase
import com.jeeldobariya.passcodes.flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.utils.appDatastore
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


class PasswordManagerActivity : AppCompatActivity() {
    private val importPasswordUseCase: ImportPasswordCSVUseCase by inject()

    private val exportPasswordUseCase: ExportPasswordCSVUseCase by inject()

    private lateinit var binding: ActivityPasswordManagerBinding

    private lateinit var exportCsvLauncher: ActivityResultLauncher<Intent>

    private lateinit var importCsvLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        importCsvLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                requireNotNull(uri)

                lifecycleScope.launch(Dispatchers.IO) {
                    importPasswordUseCase.run(uri)
                }
            }
        }

        exportCsvLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                requireNotNull(uri)

                lifecycleScope.launch(Dispatchers.IO) {
                    exportPasswordUseCase.run(uri)
                }
            }
        }

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
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "passwords.csv")
        }

        exportCsvLauncher.launch(intent)
    }

    private fun importCsvFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
            putExtra(Intent.EXTRA_TITLE, "passwords.csv")
        }

        importCsvLauncher.launch(intent)
    }
}
