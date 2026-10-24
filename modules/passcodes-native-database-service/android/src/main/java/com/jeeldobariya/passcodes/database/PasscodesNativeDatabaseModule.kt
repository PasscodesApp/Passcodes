package com.jeeldobariya.passcodes.database

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JS-facing surface of the native database module. Deliberately small
 * for now — the Autofill module talks to [PasscodesDatabase] directly
 * from Kotlin, and credential data should not cross the JS bridge more
 * than needed. This is where importer/exporter entry points will live.
 */
class PasscodesNativeDatabaseModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("PasscodesNativeDatabase")

    // Suspend-based async function: the file I/O is moved to
    // Dispatchers.IO instead of blocking the module's own thread.
    AsyncFunction("getPasswordCount") Coroutine { ->
      val context = appContext.reactContext ?: return@Coroutine 0
      withContext(Dispatchers.IO) { PasscodesDatabase(context).countPasswords() }
    }
  }
}
