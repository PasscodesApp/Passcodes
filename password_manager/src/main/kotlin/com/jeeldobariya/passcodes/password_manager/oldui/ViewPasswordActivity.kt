package com.jeeldobariya.passcodes.password_manager.oldui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.core.R
import com.jeeldobariya.passcodes.core.collectLatestLifecycleFlow
import com.jeeldobariya.passcodes.password_manager.databinding.ActivityViewPasswordBinding
import com.jeeldobariya.passcodes.password_manager.presentation.view_password.ViewPasswordAction
import com.jeeldobariya.passcodes.password_manager.presentation.view_password.ViewPasswordViewModel
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 * Activity expects id as intent parameters.
 */
class ViewPasswordActivity : AppCompatActivity() {

    private val viewModel: ViewPasswordViewModel by viewModel()

    private lateinit var binding: ActivityViewPasswordBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityViewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val passwordEntityId = intent.getIntExtra("id", -1) // -1 is an invalid id.

        if (passwordEntityId == -1) { // invalid entity
            Toast.makeText(this, getString(R.string.error_invalid_password_id), Toast.LENGTH_SHORT)
                .show()
            finish()
            return // Exit onCreate if ID is invalid
        }

        viewModel.onAction(ViewPasswordAction.LoadPassword(passwordEntityId))

        collectLatestLifecycleFlow(viewModel.state) { state ->
            binding.tvDomain.text =
                "${getString(R.string.domain_prefix)}  ${state.domain}"
            binding.tvUsername.text =
                "${getString(R.string.username_prefix)}  ${state.username}"
            binding.tvPassword.text =
                "${getString(R.string.password_prefix)}  ${state.password}"
            binding.tvNotes.text =
                "${getString(R.string.notes_prefix)}  ${state.notes}"
            binding.tvUpdatedAt.text =
                "${getString(R.string.updatedat_prefix)}  ${state.lastUpdatedAt}"

            if (state.isError) {
                Toast.makeText(
                    this@ViewPasswordActivity,
                    getString(R.string.something_went_wrong_msg),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAction(ViewPasswordAction.RefreshPassword)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.copyPasswordBtn.setOnClickListener {
            Toast.makeText(this, getString(R.string.preview_feature), Toast.LENGTH_SHORT).show()

            val confirmDialog = AlertDialog.Builder(this@ViewPasswordActivity)
                .setTitle(R.string.copy_password_dialog_title)
                .setMessage(R.string.danger_copy_to_clipboard_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which ->
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip: ClipData =
                        ClipData.newPlainText(
                            viewModel.state.value.username,
                            viewModel.state.value.password
                        )

                    // Set the ClipData to the clipboard
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, getString(R.string.copy_success), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .setNegativeButton(R.string.discard_dialog_button_text) { dialog, which ->
                    Toast.makeText(this, getString(R.string.action_discard), Toast.LENGTH_SHORT)
                        .show()
                }
                .create()

            confirmDialog.show()
        }

        binding.updatePasswordBtn.setOnClickListener {
            val viewPasswordIntent = Intent(this, UpdatePasswordActivity::class.java)
            viewPasswordIntent.putExtra("id", viewModel.passwordEntityId)
            startActivity(viewPasswordIntent)
        }

        binding.deletePasswordBtn.setOnClickListener {
            val confirmDialog = AlertDialog.Builder(this@ViewPasswordActivity)
                .setTitle(R.string.delete_password_dialog_title)
                .setMessage(R.string.irreversible_dialog_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which ->
                    runBlocking { viewModel.onAction(ViewPasswordAction.DeletePasswordAction) }
                    finish()
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
