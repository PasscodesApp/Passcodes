package com.jeeldobariya.passcodes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.BuildConfig
import com.jeeldobariya.passcodes.databinding.ActivityMainBinding
import com.jeeldobariya.passcodes.utils.CommonUtils
import com.jeeldobariya.passcodes.utils.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// import com.jeeldobariya.passcodes.utils.Permissions

class MainActivity : AppCompatActivity() {

    // private lateinit var permissionsHandle: Permissions

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CommonUtils.updateCurrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            UpdateChecker.checkVersion(this@MainActivity, BuildConfig.VERSION_NAME)
        }

        // Add event onclick listener
        addOnClickListenerOnButton()

        // Make window fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        /* Comment the code as permission is not need to write into app data dir.
            // Check and request permission when the app is first opened
            permissionsHandle = Permissions(this)
            if (!permissionsHandle.checkPermission()) permissionsHandle.requestPermission()
        */
    }

    /* Comment the code as permission is not need to write into app data dir.
        override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == Permissions.PERMISSION_REQUEST_CODE) {
                if (permissionsHandle.isPermissionGranted(grantResults)) {
                    // Permission has been granted
                    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG).show()
                } else {
                    // Permission not granted
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                }
            }
        }
    */

    // Added all the onclick event listeners
    private fun addOnClickListenerOnButton() {
        binding.passwordManagerBtn.setOnClickListener {
            val passwordManagerIntent = Intent(this, PasswordManagerActivity::class.java)
            startActivity(passwordManagerIntent)
        }

        binding.settingBtn.setOnClickListener {
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
        }

        binding.aboutUsBtn.setOnClickListener {
            val aboutUsIntent = Intent(this, AboutUsActivity::class.java)
            startActivity(aboutUsIntent)
        }
    }
}
