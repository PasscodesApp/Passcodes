package com.jeeldobariya.passcodes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt

class AuthActivity : AppCompatActivity() {

    // private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can set a simple splash layout here if you want
        // setContentView(R.layout.activity_auth)

        // executor = ContextCompat.getMainExecutor(this)

        setupBiometricPrompt()
        checkAndAuthenticate()
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticateSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // TODO: If user cancels or error occurs, "Retry" option must be there.
                    Toast.makeText(applicationContext, errString, Toast.LENGTH_SHORT).show()
                }
            })

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Passcodes")
            .setSubtitle("Use your biometric credential to continue")
            .setConfirmationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            builder.setDeviceCredentialAllowed(true)
        }

        promptInfo = builder.build()
    }

    private fun checkAndAuthenticate() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_STRONG
        }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                openEnrollmentSettings()
            }

            else -> {
                // TODO: This should not be done. Authentication should never be skipped!!
                Toast.makeText(this, "Critical Warning: Biometric Authentication Skipped!!", Toast.LENGTH_LONG).show()
                onAuthenticateSuccess()
            }
        }
    }

    private fun onAuthenticateSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openEnrollmentSettings() {
        val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            }
        } else {
            // Android 8/9/10 fallback
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        startActivity(enrollIntent)
    }
}
