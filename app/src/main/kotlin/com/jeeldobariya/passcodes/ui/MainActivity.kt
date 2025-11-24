package com.jeeldobariya.passcodes.ui

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasscodesTheme {
                MainScreen {
                    val loginIntent = Intent(this@MainActivity, com.jeeldobariya.passcodes.oldui.MainActivity::class.java)
                    startActivity(loginIntent)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navigateToOldUi: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Passcodes", fontSize = 24.sp)
            Text("You are on New Jetpack UI")

            Spacer(Modifier.padding(12.dp))

            Button(
                onClick = navigateToOldUi
            ) {
                Text("Back To Old UI", fontSize = 20.sp)
            }
            Button(
                onClick = navigateToOldUi
            ) {
                Text("Continue New UI", fontSize = 20.sp)
            }

            Spacer(Modifier.padding(12.dp))

            Text("Jetpack UI Is Under Development", fontSize = 11.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PasscodesTheme {
        MainScreen(
            navigateToOldUi = {  }
        )
    }
}
