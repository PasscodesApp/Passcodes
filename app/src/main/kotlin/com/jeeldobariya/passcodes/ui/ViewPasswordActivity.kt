package com.jeeldobariya.passcodes.ui

import android.content.ClipData;
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.databinding.ActivityViewPasswordBinding
import com.jeeldobariya.passcodes.flags.FeatureFlagManager
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Controller
import com.jeeldobariya.passcodes.utils.DatabaseOperationException
import com.jeeldobariya.passcodes.utils.DateTimeUtils
import com.jeeldobariya.passcodes.utils.PasswordNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Activity expects id as intent parameters.
 */
class ViewPasswordActivity : AppCompatActivity() {

    private var passwordEntityId: Int = 0
    private lateinit var binding: ActivityViewPasswordBinding
    private lateinit var controller: Controller
    private lateinit var passwordEntity: Password

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityViewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        passwordEntityId = intent.getIntExtra("id", -1) // -1 is an invalid id.

        if (passwordEntityId == -1) { // invalid entity
            Toast.makeText(this, getString(R.string.error_invalid_password_id), Toast.LENGTH_SHORT).show()
            finish()
            return // Exit onCreate if ID is invalid
        }

        controller = Controller(this)

        binding.copyPasswordBtn.isEnabled = FeatureFlagManager.get(this).latestFeaturesEnabled

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun fillDataInTextview() {
        lifecycleScope.launch {
            try {
                passwordEntity = controller.getPasswordById(passwordEntityId)
                withContext(Dispatchers.Main) {
                    val lastUpdatedAt = DateTimeUtils.getRelativeDays(passwordEntity.updatedAt.orEmpty(), "yyyy-MM-dd HH:mm:ss")

                    binding.tvDomain.text = "${getString(R.string.domain_prefix)}  ${passwordEntity.domain}"
                    binding.tvUsername.text = "${getString(R.string.username_prefix)}  ${passwordEntity.username}"
                    binding.tvPassword.text = "${getString(R.string.password_prefix)}  ${passwordEntity.password}"
                    binding.tvNotes.text = "${getString(R.string.notes_prefix)}  ${passwordEntity.notes}"
                    binding.tvUpdatedAt.text = "${getString(R.string.updatedat_prefix)}  ${lastUpdatedAt}"
                }
            } catch (e: PasswordNotFoundException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewPasswordActivity, e.message, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    finish()
                }
            } catch (e: DatabaseOperationException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewPasswordActivity, "${getString(R.string.something_went_wrong_msg)}: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewPasswordActivity, "${getString(R.string.something_went_wrong_msg)}: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    finish()
                }
            }
        }
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.copyPasswordBtn.setOnClickListener {
            Toast.makeText(this, getString(R.string.preview_feature), Toast.LENGTH_SHORT).show()

            val confirmDialog = AlertDialog.Builder(this@ViewPasswordActivity)
                .setTitle(R.string.copy_password_dialog_title)
                .setMessage(R.string.danger_copy_to_clipboard_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which -> 
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip: ClipData = ClipData.newPlainText(passwordEntity.username, passwordEntity.password)

                    // Set the ClipData to the clipboard
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Clipboard service not available.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.discard_dialog_button_text) { dialog, which -> 
                    Toast.makeText(this, getString(R.string.action_discard), Toast.LENGTH_SHORT).show();
                }
                .create();

            confirmDialog.show();
        }

        binding.updatePasswordBtn.setOnClickListener {
            val viewPasswordIntent = Intent(this, UpdatePasswordActivity::class.java)
            viewPasswordIntent.putExtra("id", passwordEntityId)
            startActivity(viewPasswordIntent)
        }

        binding.deletePasswordBtn.setOnClickListener {
            val confirmDialog = AlertDialog.Builder(this@ViewPasswordActivity)
                .setTitle(R.string.delete_password_dialog_title)
                .setMessage(R.string.irreversible_dialog_desc)
                .setPositiveButton(R.string.confirm_dialog_button_text) { dialog, which -> 
                    performDeletePasswordAction()
                }
                .setNegativeButton(R.string.discard_dialog_button_text) { dialog, which -> 
                    Toast.makeText(this, getString(R.string.action_discard), Toast.LENGTH_SHORT).show();
                }
                .create();

            confirmDialog.show();
        }
    }

    private fun performDeletePasswordAction() {
        lifecycleScope.launch {
            try {
                val rowsDeleted = controller.deletePassword(passwordEntityId)
                withContext(Dispatchers.Main) {
                    if (rowsDeleted > 0) {
                        Toast.makeText(this@ViewPasswordActivity, getString(R.string.delete_success_msg), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ViewPasswordActivity, getString(R.string.something_went_wrong_msg) + ": Password not found for deletion or no rows affected.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: DatabaseOperationException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewPasswordActivity, "${getString(R.string.something_went_wrong_msg)}: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewPasswordActivity, "${getString(R.string.something_went_wrong_msg)}: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fillDataInTextview()
    }
}
