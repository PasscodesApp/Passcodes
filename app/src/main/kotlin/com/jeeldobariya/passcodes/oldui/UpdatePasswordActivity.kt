package com.jeeldobariya.passcodes.oldui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivityUpdatePasswordBinding
import com.jeeldobariya.passcodes.utils.appDatastore
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
 * Activity expects id as intent parameters.
 */
class UpdatePasswordActivity : AppCompatActivity() {

    private val viewModel: UpdatePasswordViewModel by viewModel()

    private lateinit var binding: ActivityUpdatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }

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

        binding.tvId.text = "${getString(R.string.id_prefix)} ${viewModel.passwordEntityId}"

        collectLatestLifecycleFlow(viewModel.state) { state ->
            binding.inputDomain.setText(state.domain)
            binding.inputUsername.setText(state.username)
            binding.inputPassword.setText(state.password)
            binding.inputNotes.setText(state.notes)
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.updatePasswordBtn.setOnClickListener {
            viewModel.onAction(UpdatePasswordAction.OnChangeDomain(binding.inputDomain.text.toString()))
            viewModel.onAction(UpdatePasswordAction.OnChangeUsername(binding.inputUsername.text.toString()))
            viewModel.onAction(UpdatePasswordAction.OnChangePassword(binding.inputPassword.text.toString()))
            viewModel.onAction(UpdatePasswordAction.OnChangeNotes(binding.inputNotes.text.toString()))

            val confirmDialog = AlertDialog.Builder(this@UpdatePasswordActivity)
                .setTitle(R.string.update_password_dialog_title)
                .setMessage(R.string.irreversible_dialog_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which ->
                    viewModel.onAction(UpdatePasswordAction.OnUpdatePasswordButtonClick)
                    if (!viewModel.state.value.isError) {
                        finish()
                    }
                }
                .setNegativeButton(R.string.discard_dialog_button_text) { dialog, which ->
                    Toast.makeText(this, getString(R.string.action_discard), Toast.LENGTH_SHORT)
                        .show()
                }
                .create()

            confirmDialog.show()
        }
    }
}
