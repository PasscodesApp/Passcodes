package com.jeeldobariya.passcodes.oldui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.jeeldobariya.passcodes.BuildConfig
import com.jeeldobariya.passcodes.Constant
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.domain.usecases.CheckForUpdateUseCase
import com.jeeldobariya.passcodes.core.domain.usecases.UpdateCheckingResult
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.databinding.ActivityMainBinding
import com.jeeldobariya.passcodes.password_manager.oldui.PasswordManagerActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

// import com.jeeldobariya.passcodes.utils.Permissions

class MainActivity : AppCompatActivity() {

    // private lateinit var permissionsHandle: Permissions

    private lateinit var binding: ActivityMainBinding

    private val checkForUpdateUseCase: CheckForUpdateUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val result = checkForUpdateUseCase(
                currentVersion = BuildConfig.VERSION_NAME,
                githubReleaseApiUrl = Constant.GITHUB_RELEASE_API_URL,
                telegramCommunityUrl = Constant.TELEGRAM_COMMUNITY_URL
            )

            when (result) {
                UpdateCheckingResult.ON_UNOFFICIAL_RELEASE,
                UpdateCheckingResult.ON_PRE_RELEASE
                    -> {
                    Intent(this@MainActivity, UpdateCheckingActivity::class.java).also {
                        startActivity(it)
                    }
                }
                else -> Log.e("UpdateChecking", result.toString())
            }
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

        runBlocking {
            if (featureFlagsDatastore.data.first().isPreviewLayoutEnabled) {
                Intent(
                    this@MainActivity,
                    com.jeeldobariya.passcodes.MainActivity::class.java
                ).also {
                    startActivity(it)
                    finish()
                }
            }
        }
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
