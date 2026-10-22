package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Transparent, non-visible host for the Autofill "tap to unlock" step.
 *
 * The system launches this Activity (via the IntentSender set with
 * Dataset.Builder#setAuthentication) when the user taps a locked
 * credential suggestion built in PasscodesAutofillService. It shows a
 * biometric/device-credential prompt and, only on success, re-reads
 * that one credential by id and hands the real values back to the
 * Autofill framework.
 *
 * Carries no secret in its own Intent either direction — only a row id
 * and the AutofillIds to fill once unlocked. The real values are read
 * fresh from the database after authentication succeeds, never passed
 * in or cached beforehand.
 */
@RequiresApi(Build.VERSION_CODES.O)
class AutofillUnlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rowId = intent.getLongExtra(EXTRA_ROW_ID, NO_ROW_ID)
        val usernameId = intent.autofillIdExtra(EXTRA_USERNAME_AUTOFILL_ID)
        val passwordId = intent.autofillIdExtra(EXTRA_PASSWORD_AUTOFILL_ID)

        if (rowId == NO_ROW_ID || (usernameId == null && passwordId == null)) {
            finishCancelled()
            return
        }

        val biometricManager = BiometricManager.from(this)

        if (biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS) != BiometricManager.BIOMETRIC_SUCCESS) {
            // Nothing enrolled to authenticate against (no biometrics,
            // no device PIN/pattern/password set up). Rather than lock
            // the user out of their own vault, proceed as if unlocked —
            // the same call made for "no security configured at all".
            completeWithCredential(rowId, usernameId, passwordId)
            return
        }

        showBiometricPrompt(rowId, usernameId, passwordId)
    }

    private fun showBiometricPrompt(rowId: Long, usernameId: AutofillId?, passwordId: AutofillId?) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                completeWithCredential(rowId, usernameId, passwordId)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Covers user cancel, lockout, negative button, etc. —
                // all treated the same: no fill, no crash, just close.
                finishCancelled()
            }

            // onAuthenticationFailed (e.g. one unmatched fingerprint) is
            // intentionally not overridden — the system prompt stays
            // open on its own and lets the user retry.
        }

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.autofill_unlock_title))
            .setSubtitle(getString(R.string.autofill_unlock_subtitle))
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun completeWithCredential(rowId: Long, usernameId: AutofillId?, passwordId: AutofillId?) {
        val entry = PasscodesDatabase(applicationContext).getPasswordById(rowId)
        if (entry == null) {
            finishCancelled()
            return
        }

        val datasetBuilder = Dataset.Builder()
        usernameId?.let { datasetBuilder.setValue(it, AutofillValue.forText(entry.username)) }
        passwordId?.let { datasetBuilder.setValue(it, AutofillValue.forText(entry.password)) }

        val resultIntent = Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, datasetBuilder.build())
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun finishCancelled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        const val EXTRA_ROW_ID = "com.jeeldobariya.passcodes.autofill.EXTRA_ROW_ID"
        const val EXTRA_USERNAME_AUTOFILL_ID = "com.jeeldobariya.passcodes.autofill.EXTRA_USERNAME_AUTOFILL_ID"
        const val EXTRA_PASSWORD_AUTOFILL_ID = "com.jeeldobariya.passcodes.autofill.EXTRA_PASSWORD_AUTOFILL_ID"

        private const val NO_ROW_ID = -1L

        // DEVICE_CREDENTIAL lets a user with only a PIN/pattern/password
        // (no fingerprint/face enrolled) still pass the gate, rather
        // than being forced into biometrics specifically.
        val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

        private fun Intent.autofillIdExtra(key: String): AutofillId? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(key, AutofillId::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableExtra(key)
            }
    }
}
