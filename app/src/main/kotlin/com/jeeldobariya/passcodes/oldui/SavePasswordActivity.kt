package com.jeeldobariya.passcodes.oldui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivitySavePasswordBinding
import com.jeeldobariya.passcodes.utils.appDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavePasswordActivity : AppCompatActivity() {

    private val viewModel: SavePasswordViewModel by viewModel()

    private lateinit var binding: ActivitySavePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySavePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inputDomain.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputDomain.setHint(getString(R.string.placeholder_domain_field))
            } else {
                binding.inputDomain.setHint("")
            }
        }

        binding.inputUsername.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputUsername.setHint(getString(R.string.placeholder_username_field))
            } else {
                binding.inputUsername.setHint("")
            }
        }

        binding.inputPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.inputPassword.setHint(getString(R.string.placeholder_password_field))
            } else {
                binding.inputPassword.setHint("")
            }
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.savePasswordBtn.setOnClickListener {
            viewModel.onAction(SavePasswordAction.OnChangeDomain(binding.inputDomain.text.toString()))
            viewModel.onAction(SavePasswordAction.OnChangeUsername(binding.inputUsername.text.toString()))
            viewModel.onAction(SavePasswordAction.OnChangePassword(binding.inputPassword.text.toString()))
            viewModel.onAction(SavePasswordAction.OnChangeNotes(binding.inputNotes.text.toString()))

            viewModel.onAction(SavePasswordAction.OnSavePasswordButtonClick)

            if (!viewModel.state.value.isError) {
                finish()
            }
        }
    }
}
