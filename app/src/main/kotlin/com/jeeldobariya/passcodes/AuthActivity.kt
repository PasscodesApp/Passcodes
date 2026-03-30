package com.jeeldobariya.passcodes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.widget.TextViewCompat
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme

class AuthActivity : AppCompatActivity() {

    // private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can set a simple splash layout here if you want
        // setContentView(R.layout.activity_auth)

        // executor = ContextCompat.getMainExecutor(this)

        setContent {
            PasscodesTheme {
                AuthScreenContent()
            }
        }
    }

    @Composable
    private fun AuthScreenContent() {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val punchline = listOf(
                    "How Can I Trust You?",
                    "Authentication",
                    "How Can I Trust You? 🧐",
                    "Identity confirmed... or is it? 🕵️‍♂️",
                    "I Dare You: Prove You Are You! ⚡",
                    "I Dare You, Tell Me You Are You!!!",
                    "Prove you aren't a sophisticated bot.",
                    "Are you really you today?",
                    "Authentication",
                    "Are you really you today? ✨",
                    "Access requires a pulse 🫀",
                    "Identity confirmed... or is it?",
                    "Authentication",
                    "The vault demands a thumb! 👍",
                    "Nice face. Can I see some ID? 📸",
                    "I hope you are genuine"
                ).random()
                Text(punchline, autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 36.sp), style = LocalTextStyle.current.copy(lineHeight = 1.2.em), textAlign = TextAlign.Center)
                Text("Preview Feature", fontSize = 16.sp)

                Spacer(modifier = Modifier.padding(24.dp))

                Button(
                    onClick = {
                        setupBiometricPrompt()
                        checkAndAuthenticate()
                    }
                ) {
                    Text("Unlock")
                }

                Spacer(modifier = Modifier.padding(12.dp))

                Text("(disable by toggle of preview features from settings)", fontSize = 11.sp)
            }
        }
    }

    @PreviewLightDark
    @Composable
    private fun AuthScreenContentPreview() {
        PasscodesTheme {
            AuthScreenContent()
        }
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
