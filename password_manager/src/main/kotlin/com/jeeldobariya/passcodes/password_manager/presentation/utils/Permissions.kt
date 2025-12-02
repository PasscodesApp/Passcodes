package com.jeeldobariya.passcodes.password_manager.presentation.utils

// comment the below code as permission are not used in app.
// currently, but we might require this later..
/*
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions(private val activity: Activity) {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    /**
     * Check if write and read external storage permissions have been granted.
     * @return True if permissions are granted, false otherwise.
     */
    fun checkPermission(): Boolean {
        val resultWrite =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val resultRead =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        return resultWrite == PackageManager.PERMISSION_GRANTED && resultRead == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request write and read external storage permissions from the user.
     */
    fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Check if permissions were granted after a request.
     * This method is typically called in [Activity.onRequestPermissionsResult].
     * @param grantResults The results for the requested permissions.
     * @return True if both requested permissions were granted, false otherwise.
     */
    fun isPermissionGranted(grantResults: IntArray): Boolean {
        // We expect two permissions (WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
        // and check if both were granted.
        return grantResults.isNotEmpty() &&
                grantResults.size >= 2 && // Ensure we got results for at least two permissions
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
    }
}
*/
