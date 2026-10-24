package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
import com.jeeldobariya.passcodes.autofill.settings.AutofillPreferences
import com.jeeldobariya.passcodes.autofill.settings.AutofillSettingsActivity
import com.jeeldobariya.passcodes.autofill.settings.FillMode
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition


class PasscodesAutofillServiceModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("PasscodesAutofillService")

    Function("isAutofillServiceEnabled") {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val context = appContext.reactContext ?: return@Function false
        val autofillManager = context.getSystemService(AutofillManager::class.java)
        return@Function autofillManager?.hasEnabledAutofillServices() ?: false
      }
      return@Function false
    }

    Function("openAutofillSettings") {
      val context = appContext.reactContext ?: return@Function Unit
      val intent = Intent(context, AutofillSettingsActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }

    // -------

    // The functions below all read/write the same DataStore file that
    // PasscodesAutofillService and AutofillSettingsActivity use — one
    // source of truth regardless of which side changes it.

    // Settings functions are suspend-based (`Coroutine`) rather than
    // wrapping runBlocking: DataStore is already asynchronous, so the
    // module thread is never blocked waiting on disk. Setters resolve to
    // the value read back after the write.

    AsyncFunction("isInlineSuggestionsEnabled") Coroutine { ->
      val context = appContext.reactContext ?: return@Coroutine true
      AutofillPreferences.isInlineSuggestionsEnabled(context)
    }

    AsyncFunction("setInlineSuggestionsEnabled") Coroutine { enabled: Boolean ->
      val context = appContext.reactContext ?: return@Coroutine enabled
      AutofillPreferences.setInlineSuggestionsEnabled(context, enabled)
    }

    AsyncFunction("isBiometricLockEnabled") Coroutine { ->
      val context = appContext.reactContext ?: return@Coroutine false
      AutofillPreferences.isBiometricLockEnabled(context)
    }

    AsyncFunction("setBiometricLockEnabled") Coroutine { enabled: Boolean ->
      val context = appContext.reactContext ?: return@Coroutine enabled
      AutofillPreferences.setBiometricLockEnabled(context, enabled)
    }

    // "smart" | "auto" | "dumb" — see FillMode.
    AsyncFunction("getFillMode") Coroutine { ->
      val context = appContext.reactContext ?: return@Coroutine FillMode.DEFAULT.storageValue
      AutofillPreferences.getFillMode(context).storageValue
    }

    AsyncFunction("setFillMode") Coroutine { mode: String ->
      val requested = FillMode.fromStorage(mode)
      val context = appContext.reactContext ?: return@Coroutine requested.storageValue
      AutofillPreferences.setFillMode(context, requested).storageValue
    }
  }
}
