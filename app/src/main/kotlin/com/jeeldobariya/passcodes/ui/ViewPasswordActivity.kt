package com.jeeldobariya.passcodes.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.databinding.ActivityViewPasswordBinding
import com.jeeldobariya.passcodes.flags.FeatureFlagManager
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

/*
 * Activity expects id as intent parameters.
 */
class ViewPasswordActivity : AppCompatActivity() {

    private val viewModel: ViewPasswordViewModel by viewModel()

    private lateinit var binding: ActivityViewPasswordBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivityViewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val passwordEntityId  = intent.getIntExtra("id", -1) // -1 is an invalid id.

        if (passwordEntityId == -1) { // invalid entity
            Toast.makeText(this, getString(R.string.error_invalid_password_id), Toast.LENGTH_SHORT)
                .show()
            finish()
            return // Exit onCreate if ID is invalid
        }

        viewModel.loadInitialData(passwordEntityId)

        collectLatestLifecycleFlow(viewModel.domainState) { domain ->
            binding.tvDomain.text =
                "${getString(R.string.domain_prefix)}  $domain"
        }
        collectLatestLifecycleFlow(viewModel.usernameState) { username ->
            binding.tvUsername.text =
                "${getString(R.string.username_prefix)}  $username"
        }
        collectLatestLifecycleFlow(viewModel.passwordState) { password ->
            binding.tvPassword.text =
                "${getString(R.string.password_prefix)}  $password"
        }
        collectLatestLifecycleFlow(viewModel.notesState) { notes ->
            binding.tvNotes.text =
                "${getString(R.string.notes_prefix)}  $notes"
        }
        collectLatestLifecycleFlow(viewModel.lastUpdatedAtState) { lastUpdatedAt ->
            binding.tvUpdatedAt.text =
                "${getString(R.string.updatedat_prefix)}  $lastUpdatedAt"
        }
        collectLatestLifecycleFlow(viewModel.isErrorState) { error ->
            if (error) {
                Toast.makeText(
                    this@ViewPasswordActivity,
                    getString(R.string.something_went_wrong_msg),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        binding.copyPasswordBtn.isEnabled = FeatureFlagManager.get(this).latestFeaturesEnabled

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
                        ClipData.newPlainText(viewModel.usernameState.value, viewModel.passwordState.value)

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
                    runBlocking {  viewModel.onDeletePasswordButtonClick() }
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
