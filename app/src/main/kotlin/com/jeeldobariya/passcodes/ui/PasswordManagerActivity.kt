package com.jeeldobariya.passcodes.ui

import android.content.Intent
import android.view.View.GONE
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.withContext


class PasswordManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordManagerBinding
    private lateinit var controller: Controller

    private lateinit var exportCsvLauncher: ActivityResultLauncher<Intent>
    private var tmpExportCSVData: String? = null

    private lateinit var importCsvLauncher: ActivityResultLauncher<Intent>

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

        importCsvLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val CSVData: String? = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    }

                    lifecycleScope.launch(Dispatchers.IO) {
                        if (CSVData != null) {
                            try {
                                val result: IntArray = controller.importDataFromCsvString(CSVData)

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@PasswordManagerActivity,
                                        getString(R.string.import_success, result[0]),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    
                                    if (result[1] != 0) {
                                        Toast.makeText(
                                            this@PasswordManagerActivity,
                                            getString(R.string.import_failed, result[1]),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@PasswordManagerActivity,
                                        e.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        exportCsvLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null && !tmpExportCSVData.isNullOrEmpty()) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(tmpExportCSVData!!.toByteArray())
                    }
                    Toast.makeText(this, getString(R.string.export_success), Toast.LENGTH_SHORT).show()
                }
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
            Toast.makeText(this@PasswordManagerActivity, getString(R.string.preview_feature), Toast.LENGTH_LONG).show()

            importCsvFilePicker()
        }

        binding.exportPasswordBtn.setOnClickListener {
            Toast.makeText(this@PasswordManagerActivity, getString(R.string.preview_feature), Toast.LENGTH_LONG).show()

            lifecycleScope.launch(Dispatchers.IO) {
                val csvDataExportBlob = controller.exportDataToCsvString()

                withContext(Dispatchers.Main) {
                    tmpExportCSVData = csvDataExportBlob
                    exportCsvFilePicker()
                }
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
