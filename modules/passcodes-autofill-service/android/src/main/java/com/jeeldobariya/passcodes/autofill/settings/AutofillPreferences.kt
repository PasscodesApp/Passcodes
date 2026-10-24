package com.jeeldobariya.passcodes.autofill.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Small Autofill-related settings, stored in a DataStore Preferences
 * file rather than the passwords DB — this is app configuration, not
 * credential data, and keeping it out of the SQLite file means no
 * schema coupling for something this simple.
 *
 * Every side reads/writes the same file:
 *  - React Native, through PasscodesAutofillServiceModule's bridge
 *    functions (suspend-friendly via Expo's AsyncFunction).
 *  - AutofillSettingsActivity's in-app toggles.
 *  - PasscodesAutofillService's fill-request handling.
 *
 * DataStore handles concurrent-access safety on its own, so none of
 * these call sites need to coordinate beyond just calling these
 * functions.
 */
private val Context.autofillDataStore by preferencesDataStore(name = "passcodes_autofill_settings")

object AutofillPreferences {

    private val KEY_INLINE_SUGGESTIONS_ENABLED = booleanPreferencesKey("inline_suggestions_enabled")
    private val KEY_BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")

    private val KEY_FILL_MODE = stringPreferencesKey("fill_mode")

    private const val DEFAULT_INLINE_SUGGESTIONS_ENABLED = true
    private const val DEFAULT_BIOMETRIC_LOCK_ENABLED = false

    // -- Inline suggestions -------------------------------------------

    suspend fun isInlineSuggestionsEnabled(context: Context): Boolean =
        context.autofillDataStore.data.first()[KEY_INLINE_SUGGESTIONS_ENABLED]
            ?: DEFAULT_INLINE_SUGGESTIONS_ENABLED

    suspend fun setInlineSuggestionsEnabled(
        context: Context,
        enabled: Boolean
    ): Boolean {
        context.autofillDataStore.edit { prefs ->
            prefs[KEY_INLINE_SUGGESTIONS_ENABLED] = enabled
        }

        return isInlineSuggestionsEnabled(context)
    }

    // -- Biometric lock on fill -----------------------------------------

    suspend fun isBiometricLockEnabled(context: Context): Boolean =
        context.autofillDataStore.data.first()[KEY_BIOMETRIC_LOCK_ENABLED]
            ?: DEFAULT_BIOMETRIC_LOCK_ENABLED
    
    suspend fun setBiometricLockEnabled(
        context: Context,
        enabled: Boolean
    ): Boolean {
        context.autofillDataStore.edit { prefs -> 
            prefs[KEY_BIOMETRIC_LOCK_ENABLED] = enabled
        }

        return isBiometricLockEnabled(context)
    }

    // -- Fill mode (smart / auto / dumb) --------------------------------

    suspend fun getFillMode(context: Context): FillMode =
        FillMode.fromStorage(context.autofillDataStore.data.first()[KEY_FILL_MODE])

    suspend fun setFillMode(context: Context, mode: FillMode): FillMode {
        context.autofillDataStore.edit { prefs ->
            prefs[KEY_FILL_MODE] = mode.storageValue
        }

        return getFillMode(context)
    }

    // -- Blocking convenience for non-coroutine call sites ---------------
    //
    // Only call these from a background thread. PasscodesAutofillService
    // already hops off the main thread before touching anything DB/
    // DataStore-related; AutofillSettingsActivity does its own small
    // background hop around these (see its loadPreferences()/persist()).

    fun isInlineSuggestionsEnabledBlocking(context: Context): Boolean =
        runBlocking { isInlineSuggestionsEnabled(context) }

    fun setInlineSuggestionsEnabledBlocking(context: Context, enabled: Boolean) =
        runBlocking { setInlineSuggestionsEnabled(context, enabled) }

    fun isBiometricLockEnabledBlocking(context: Context): Boolean =
        runBlocking { isBiometricLockEnabled(context) }

    fun setBiometricLockEnabledBlocking(context: Context, enabled: Boolean) =
        runBlocking { setBiometricLockEnabled(context, enabled) }

    fun getFillModeBlocking(context: Context): FillMode =
        runBlocking { getFillMode(context) }

    fun setFillModeBlocking(context: Context, mode: FillMode) =
        runBlocking { setFillMode(context, mode) }
}
