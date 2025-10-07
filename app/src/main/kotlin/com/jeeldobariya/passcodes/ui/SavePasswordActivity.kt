package com.jeeldobariya.passcodes.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivitySavePasswordBinding
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Controller
import com.jeeldobariya.passcodes.utils.DatabaseOperationException
import com.jeeldobariya.passcodes.utils.InvalidInputException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavePasswordActivity : AppCompatActivity() {

    private lateinit var controller: Controller
    private lateinit var binding: ActivitySavePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySavePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Controller(this) // Initialize controller

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.inputDomain.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputDomain.setHint(getString(R.string.placeholder_domain_field));
            } else {
                binding.inputDomain.setHint("");
            }
        };

        binding.inputUsername.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputUsername.setHint(getString(R.string.placeholder_username_field));
            } else {
                binding.inputUsername.setHint("");
            }
        };

        binding.inputPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputPassword.setHint(getString(R.string.placeholder_password_field));
            } else {
                binding.inputPassword.setHint("");
            }
        };

        binding.savePasswordBtn.setOnClickListener {
            val domain = binding.inputDomain.text.toString()
            val username = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()
            val notes = binding.inputNotes.text.toString()

            performSavePasswordAction(domain, username, password, notes)
        }
    }

    fun performSavePasswordAction(
        domain: String,
        username: String,
        password: String,
        notes: String
    ) {
        // Launch a coroutine to call the suspend function
        lifecycleScope.launch {
            try {
                val rowId = controller.savePasswordEntity(domain, username, password, notes)
                // Switch back to Main dispatcher for UI updates
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavePasswordActivity,
                        "${getString(R.string.success_clause)} $rowId",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: InvalidInputException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavePasswordActivity,
                        getString(R.string.warn_fill_form),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: DatabaseOperationException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavePasswordActivity,
                        "${getString(R.string.fail_msg)}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavePasswordActivity,
                        "${getString(R.string.fail_msg)}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
