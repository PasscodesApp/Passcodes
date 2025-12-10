package com.jeeldobariya.passcodes.password_manager.oldui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.password_manager.R
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.utils.collectLatestLifecycleFlow
import com.jeeldobariya.passcodes.password_manager.databinding.ActivityLoadPasswordBinding
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.oldui.adapter.PasswordAdapter
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.LoadPasswordAction
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.LoadPasswordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoadPasswordActivity : AppCompatActivity() {

    private val viewModel: LoadPasswordViewModel by viewModel()

    private lateinit var binding: ActivityLoadPasswordBinding
    private lateinit var passwordAdapter: PasswordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoadPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        collectLatestLifecycleFlow(viewModel.state) { state ->
            if (!this@LoadPasswordActivity::passwordAdapter.isInitialized) {
                passwordAdapter =
                    PasswordAdapter(this@LoadPasswordActivity, state.passwordEntityList)
                binding.passwordList.adapter = passwordAdapter
            } else {
                passwordAdapter.updateData(state.passwordEntityList)
            }

            if (state.isError) {
                Toast.makeText(
                    this@LoadPasswordActivity,
                    getString(R.string.something_went_wrong_msg),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onResume() {
        super.onResume()

        viewModel.onAction(LoadPasswordAction.RefreshPassword)
    }

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.passwordList.setOnItemClickListener { _, _, position, _ ->
            // getItem returns Any, so we cast it to Password
            val selectedPassword = passwordAdapter.getItem(position) as PasswordModal

            // Do something with the selectedPassword
            val intent = Intent(this, ViewPasswordActivity::class.java)
            intent.putExtra("id", selectedPassword.id) // Pass the ID from the Password entity
            startActivity(intent)
        }
    }
}
