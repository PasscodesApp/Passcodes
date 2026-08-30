package com.jeeldobariya.passcodes.autofill

import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
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
      val context =
          appContext.reactContext
              ?: return@Function Unit

      val intent = Intent(
          context,
          AutofillSettingsActivity::class.java
      ).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(intent)
    }
  }
}
