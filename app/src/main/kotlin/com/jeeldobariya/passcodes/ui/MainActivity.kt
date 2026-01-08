package com.jeeldobariya.passcodes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasscodesTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.app_name), fontSize = 32.sp)

            Text(stringResource(R.string.app_version), fontSize = 12.sp)

            Spacer(Modifier.padding(48.dp))

            Button(onClick = { /* TODO */ }) {
                Text(stringResource(R.string.password_manager_button_text))
            }

            FilledTonalButton(onClick = { /* TODO */ }) {
                Text(stringResource(R.string.setting_button_text))
            }

            FilledTonalButton(onClick = { /* TODO */ }) {
                Text(stringResource(R.string.about_us_button_text))
            }
        }
    }
}

@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun MainScreenPreview() {
    PasscodesTheme {
        MainScreen()
    }
}
