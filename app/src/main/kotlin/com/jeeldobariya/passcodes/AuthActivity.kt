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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import kotlinx.coroutines.flow.first
import androidx.core.net.toUri
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AuthActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var showDeprecationDialog by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PasscodesTheme {

                LaunchedEffect(showDeprecationDialog) {
                    if (!showDeprecationDialog && !featureFlagsDatastore.data.first().isPreviewFeaturesEnabled) {
                        onAuthenticateSuccess()
                    }
                }

                if (showDeprecationDialog) {
                    DeprecationDialog(
                        onDismiss = { showDeprecationDialog = false },
                        onUpdate = {
                            Intent(Intent.ACTION_VIEW,
                                Constant.WEBSITE_URL.toUri()).also {
                                startActivity(it)
                            }
                        }
                    )
                }

                AuthScreenContent()
            }
        }
    }

    @Composable
    private fun DeprecationDialog(onDismiss: () -> Unit, onUpdate: () -> Unit) {
        AlertDialog(
            onDismissRequest = { /* Rigid: user must choose an action */ },
            title = {
                Column {
                    Text(
                        text = " 🛑 End of Support Notice",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "As of now & forwards, we are officially deprecating all versions v2.x.x and prior.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "To ensure your data remains secure and you keep access to your passcodes, please update to the latest version immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Version 3.0.0 is coming soon",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), // Explicit bold
                        color = MaterialTheme.colorScheme.primary // Use primary to highlight the upcoming good news
                    )

                    Text(
                        text = "END OF SUPPORT: May 25, 2026",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Latest Version")
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Continue with Legacy Version (Unsafe)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            dismissButton = null
        )
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
                Text(
                    text = punchline,
                    // Auto-sizing works best when you give it a clear constraint
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 18.sp,
                        maxFontSize = 40.sp
                    ),
                    style = LocalTextStyle.current.copy(
                        // 1.1em to 1.2em is the "sweet spot" for auto-sized text with emojis
                        lineHeight = 1.15.em,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp // Adds a modern, "tight" look to large headers
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Preview Feature 🛠️",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.padding(36.dp))

                Button(
                    onClick = {
                        setupBiometricPrompt()
                        checkAndAuthenticate()
                    }
                ) {
                    Text("Unlock")
                }

                TextButton(
                    onClick = {
                        finish()
                    }
                ) {
                    Text("Oops, I come here, by mistakenly!!", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.padding(16.dp))

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
