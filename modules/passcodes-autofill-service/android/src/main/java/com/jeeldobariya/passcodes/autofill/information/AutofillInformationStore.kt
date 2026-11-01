package com.jeeldobariya.passcodes.autofill.information

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Where the user's Autofill personal details live: a Preferences
 * DataStore of their own, deliberately separate from the settings file
 * ("passcodes_autofill_settings") and from the Drizzle-managed
 * passwords database.
 *
 *  - React Native reads/writes it through the module's
 *    getAutofillInformation / setAutofillInformation bridge functions.
 *  - The Autofill service and unlock screen read it directly.
 *
 * The whole record is kept as one JSON string under a single key, so an
 * update is atomic and a half-written record can't exist.
 *
 * Not encrypted yet — Android's app sandbox is the only protection.
 * Everything funnels through this object, so encryption can be added in
 * [encode] / [decode] later without touching any caller. This data is
 * local to the device: it is not part of the database and is not
 * covered by import/export.
 *
 * Privacy: nothing here ever logs the data, and exception messages
 * (which can quote the JSON) are never logged — only the exception type.
 */
private val Context.autofillInformationDataStore by preferencesDataStore(name = "passcodes_autofill_information")

object AutofillInformationStore {

    private const val TAG = "AutofillInformation"
    private val KEY_INFORMATION = stringPreferencesKey("information_v1")

    /** The stored details, or an empty record if none (or unreadable). */
    suspend fun get(context: Context): AutofillInformation {
        val raw = context.autofillInformationDataStore.data.first()[KEY_INFORMATION]
            ?: return AutofillInformation()
        return decode(raw)
    }

    /** Validates and saves [information]; returns what was actually stored. */
    suspend fun set(context: Context, information: AutofillInformation): AutofillInformation {
        val clean = information.sanitized()
        context.autofillInformationDataStore.edit { prefs ->
            if (clean.isEmpty) prefs.remove(KEY_INFORMATION) else prefs[KEY_INFORMATION] = encode(clean)
        }
        return get(context)
    }

    suspend fun clear(context: Context): AutofillInformation {
        context.autofillInformationDataStore.edit { it.remove(KEY_INFORMATION) }
        return get(context)
    }

    // -- Serialisation (the one place to add encryption later) ---------

    private fun encode(information: AutofillInformation): String =
        JSONObject(information.toMap()).toString()

    private fun decode(raw: String): AutofillInformation = try {
        AutofillInformation.fromMap(toKotlin(JSONObject(raw)) as? Map<*, *>)
    } catch (e: Exception) {
        Log.e(TAG, "Stored information could not be read (${e.javaClass.simpleName})")
        AutofillInformation()
    }

    private fun toKotlin(value: Any?): Any? = when (value) {
        is JSONObject -> value.keys().asSequence().associateWith { toKotlin(value.opt(it)) }
        is JSONArray -> (0 until value.length()).map { toKotlin(value.opt(it)) }
        JSONObject.NULL -> null
        else -> value
    }
}
