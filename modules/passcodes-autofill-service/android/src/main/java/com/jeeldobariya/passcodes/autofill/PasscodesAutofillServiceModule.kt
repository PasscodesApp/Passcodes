package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
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

    // Shared with the native Autofill service via AutofillPreferences —
    // reading/writing here changes the exact same DataStore file the
    // fill-request path reads from.
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
  }
}
