package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

import com.jeeldobariya.passcodes.autofill.settings.AutofillPreferences
import com.jeeldobariya.passcodes.autofill.settings.AutofillSettingsActivity
import com.jeeldobariya.passcodes.autofill.settings.FillMode


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

    AsyncFunction("isInlineSuggestionsEnabled") {
      val context = appContext.reactContext ?: return@AsyncFunction true
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.isInlineSuggestionsEnabled(context)
      }
    }

    AsyncFunction("setInlineSuggestionsEnabled") { enabled: Boolean ->
      val context = appContext.reactContext
        ?: return@AsyncFunction enabled

      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setInlineSuggestionsEnabled(
          context,
          enabled
        )
      }
    }

    AsyncFunction("isBiometricLockEnabled") {
      val context = appContext.reactContext ?: return@AsyncFunction true
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.isBiometricLockEnabled(context)
      }
    }

    AsyncFunction("setBiometricLockEnabled") { enabled: Boolean ->
      val context = appContext.reactContext
        ?: return@AsyncFunction enabled

      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setBiometricLockEnabled(
          context,
          enabled
        )
      }
    }

    // "smart" | "auto" | "dumb" — see FillMode.
    AsyncFunction("getFillMode") {
      val context = appContext.reactContext ?: return@AsyncFunction FillMode.DEFAULT.storageValue
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.getFillMode(context).storageValue
      }
    }

    AsyncFunction("setFillMode") { mode: String ->
      val requested = FillMode.fromStorage(mode)
      val context = appContext.reactContext
        ?: return@AsyncFunction requested.storageValue

      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setFillMode(context, requested).storageValue
      }
    }
  }
}
