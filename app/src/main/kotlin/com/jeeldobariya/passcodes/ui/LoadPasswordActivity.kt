package com.jeeldobariya.passcodes.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.databinding.ActivityLoadPasswordBinding
import com.jeeldobariya.passcodes.ui.adapter.PasswordAdapter
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.collectLatestLifecycleFlow
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoadPasswordActivity : AppCompatActivity() {

    private val viewModel: LoadPasswordViewModel by viewModel()

    private lateinit var binding: ActivityLoadPasswordBinding
    private lateinit var passwordAdapter: PasswordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
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

        viewModel.onAction(LoadPasswordAction.RefreshPassswordData)
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
}
