package com.jeeldobariya.passcodes.ui

import android.os.Bundle
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivityUpdatePasswordBinding
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
 * Activity expects id as intent parameters.
 */
class UpdatePasswordActivity : AppCompatActivity() {

    private val viewModel: UpdatePasswordViewModel by viewModel()

    private lateinit var binding: ActivityUpdatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val passwordEntityId = intent.getIntExtra("id", -1) // -1 is an invalid id.

        if (passwordEntityId == -1) { // invalid entity
            Toast.makeText(this, getString(R.string.error_invalid_password_id), Toast.LENGTH_SHORT)
                .show()
            finish()
            return // Exit onCreate if ID is invalid
        }

        viewModel.loadInitialData(passwordEntityId)

        binding.tvId.setText("${getString(R.string.id_prefix)} ${viewModel.passwordEntityId}")

        collectLatestLifecycleFlow(viewModel.domainState) { domain ->
            binding.inputDomain.setText(domain)
        }
        collectLatestLifecycleFlow(viewModel.usernameState) { username ->
            binding.inputUsername.setText(username)
        }
        collectLatestLifecycleFlow(viewModel.passwordState) { password ->
            binding.inputPassword.setText(password)
        }
        collectLatestLifecycleFlow(viewModel.notesState) { notes ->
            binding.inputNotes.setText(notes)
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.updatePasswordBtn.setOnClickListener {
            viewModel.onChangeDomainText(binding.inputDomain.text.toString())
            viewModel.onChangeUsernameText(binding.inputUsername.text.toString())
            viewModel.onChangePasswordText(binding.inputPassword.text.toString())
            viewModel.onChangeNotesText(binding.inputNotes.text.toString())

            val confirmDialog = AlertDialog.Builder(this@UpdatePasswordActivity)
                .setTitle(R.string.update_password_dialog_title)
                .setMessage(R.string.irreversible_dialog_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which ->
                    viewModel.onUpdatePasswordButtonClick()
                }
                .setNegativeButton(R.string.discard_dialog_button_text) { dialog, which ->
                    Toast.makeText(this, getString(R.string.action_discard), Toast.LENGTH_SHORT)
                        .show();
                }
                .create()

            confirmDialog.show();
        }
    }
}
