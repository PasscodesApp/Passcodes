package com.jeeldobariya.passcodes.autofill

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return@Function Unit
      }

      val reactContext = appContext.reactContext ?: return@Function Unit
      val packageName = reactContext.packageName

      // Using Uri.parse instead of .toUri() avoids extra KTX dependency resolution issues
      val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
        data = Uri.parse("package:$packageName")
      }

      val activity = appContext.currentActivity
      if (activity != null) {
        activity.startActivity(intent)
      } else {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactContext.startActivity(intent)
      }
    }
  }
}
