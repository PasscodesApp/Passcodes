package com.jeeldobariya.passcodes.oldui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jeeldobariya.passcodes.databinding.ActivityLicenseBinding
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader

class LicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }
        super.onCreate(savedInstanceState)
        val binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val inputStream = assets.open("LICENSE.txt")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val licenseText = bufferedReader.use { it.readText() }
            binding.licenseTextView.text = licenseText
        } catch (e: Exception) {
            binding.licenseTextView.text = "Error loading license file."
            e.printStackTrace()
        }

        binding.thirdPartyBtn.setOnClickListener { v ->
            val thirdPartyLicenseActivity = Intent(this, OssLicensesMenuActivity::class.java)
            startActivity(thirdPartyLicenseActivity)
        }
    }
}
