package com.jeeldobariya.passcodes.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.databinding.ActivityLoadPasswordBinding
import com.jeeldobariya.passcodes.ui.adapter.PasswordAdapter
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.Controller
import com.jeeldobariya.passcodes.utils.DatabaseOperationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch

class LoadPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadPasswordBinding
    private lateinit var passwordAdapter: PasswordAdapter
    private lateinit var controller: Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoadPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Controller(this) // Initialize the controller here

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Start collecting the password list Flow when the activity is created
        // This collection will automatically update the UI when database changes occur.
        collectPasswordList()
    }

    private fun collectPasswordList() {
        lifecycleScope.launch {
            controller.getAllPasswords()
                .catch { e ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoadPasswordActivity,
                            "${getString(R.string.something_went_wrong_msg)}: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                        // Ensure adapter is initialized with empty list on error
                        if (!this@LoadPasswordActivity::passwordAdapter.isInitialized) {
                            passwordAdapter =
                                PasswordAdapter(this@LoadPasswordActivity, emptyList())
                            binding.passwordList.adapter = passwordAdapter
                        }
                    }
                }
                .collect { passwordList ->
                    // This block will be executed every time the list of passwords changes in the database
                    // and emitted by the Flow.
                    withContext(Dispatchers.Main) { // Ensure UI updates are on the main thread
                        if (!this@LoadPasswordActivity::passwordAdapter.isInitialized) {
                            passwordAdapter =
                                PasswordAdapter(this@LoadPasswordActivity, passwordList)
                            binding.passwordList.adapter = passwordAdapter
                        } else {
                            passwordAdapter.updateData(passwordList)
                        }
                    }
                }
        }
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.passwordList.setOnItemClickListener { _, _, position, _ ->
            // getItem returns Any, so we cast it to Password
            val selectedPassword = passwordAdapter.getItem(position) as Password

            // Do something with the selectedPassword
            val intent = Intent(this, ViewPasswordActivity::class.java)
            intent.putExtra("id", selectedPassword.id) // Pass the ID from the Password entity
            startActivity(intent)
        }
    }

    // onResume is no longer needed to explicitly call fillPasswordList()
    // because Flow collection started in onCreate will handle updates.
    // However, if your activity might be killed and recreated, the onCreate will
    // re-initiate the collection. For simple cases, this is fine.
    // If you need to stop collection when the activity goes to background,
    // and restart on foreground, you might manage the coroutine job more explicitly.
    // But lifecycleScope handles stopping on destroy for you.
    // In this specific setup, `onResume`'s `super.onResume()` is enough.
    // No need for a custom `onResume` override anymore if it only contained `fillPasswordList()`
    // and `collectPasswordList()` is in `onCreate`.
    // Removed the `onResume` override as it's now redundant with Flow collection in onCreate.
}
