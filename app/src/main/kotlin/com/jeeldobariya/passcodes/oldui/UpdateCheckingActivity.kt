package com.jeeldobariya.passcodes.oldui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.databinding.ActivityUpdateCheckingBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class UpdateCheckingActivity: AppCompatActivity() {

    private lateinit var binding: ActivityUpdateCheckingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            setTheme(appDatastore.data.first().theme)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCheckingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}