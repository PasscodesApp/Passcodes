package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
import com.jeeldobariya.passcodes.autofill.information.AutofillInformation
import com.jeeldobariya.passcodes.autofill.information.AutofillInformationStore
import com.jeeldobariya.passcodes.autofill.settings.AutofillPreferences
import com.jeeldobariya.passcodes.autofill.settings.AutofillSettingsActivity
import com.jeeldobariya.passcodes.autofill.settings.FillMode
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


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

    AsyncFunction("isInlineSuggestionsEnabled") { ->
      val context = appContext.reactContext ?: return@AsyncFunction true
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.isInlineSuggestionsEnabled(context)
      }
    }

    AsyncFunction("setInlineSuggestionsEnabled") { enabled: Boolean ->
      val context = appContext.reactContext ?: return@AsyncFunction enabled
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setInlineSuggestionsEnabled(context, enabled)
      }
    }

    AsyncFunction("isBiometricLockEnabled") { ->
      val context = appContext.reactContext ?: return@AsyncFunction false
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.isBiometricLockEnabled(context)
      }
    }

    AsyncFunction("setBiometricLockEnabled") { enabled: Boolean ->
      val context = appContext.reactContext ?: return@AsyncFunction enabled
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setBiometricLockEnabled(context, enabled)
      }
    }

    // "smart" | "auto" | "dumb" — see FillMode.
    AsyncFunction("getFillMode") { ->
      val context = appContext.reactContext ?: return@AsyncFunction FillMode.DEFAULT.storageValue
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.getFillMode(context).storageValue
      }
    }

    AsyncFunction("setFillMode") { mode: String ->
      val requested = FillMode.fromStorage(mode)
      val context = appContext.reactContext ?: return@AsyncFunction requested.storageValue
      runBlocking(Dispatchers.IO) {
        AutofillPreferences.setFillMode(context, requested).storageValue
      }
    }

    // -------
    // Personal details for form filling (phone, email, name, birth date,
    // address). Stored in their own DataStore — never in the Drizzle
    // database, and not covered by import/export. See
    // AutofillInformationStore.
    //
    // Entry happens on the native Autofill settings screen (see
    // AutofillSettingsActivity) — not from React Native — so this app's
    // own screens don't need to collect or validate it. These functions
    // stay in case RN ever wants to read the stored details (e.g. to
    // show a "configured" badge); they're never used to write them.

    AsyncFunction("getAutofillInformation") { ->
      val context = appContext.reactContext ?: return@AsyncFunction AutofillInformation().toMap()
      runBlocking(Dispatchers.IO) {
        AutofillInformationStore.get(context).toMap()
      }
    }

    AsyncFunction("clearAutofillInformation") { ->
      val context = appContext.reactContext ?: return@AsyncFunction AutofillInformation().toMap()
      runBlocking(Dispatchers.IO) {
        AutofillInformationStore.clear(context).toMap()
      }
    }
  }
}
