package com.jeeldobariya.passcodes.autofill.security

import android.app.KeyguardManager
import android.content.Context

object DeviceSecurity {

    /**
     * True when the device has a secure lock screen (PIN, pattern,
     * password or biometric). Personal-information Autofill is switched
     * off without one: with no lock screen anyone holding the phone
     * could fill the details anywhere, and there is nothing to
     * authenticate against.
     */
    fun isDeviceSecure(context: Context): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true
}
