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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasscodesTheme {
                MainScreen {
                    featureFlagsDatastore.updateData {
                        it.copy(isPreviewLayoutEnabled = false)
                    }

                    finishAndRemoveTask()
                    exitProcess(0)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navigateToOldUi: suspend () -> Unit) {
    val scope = rememberCoroutineScope()

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
                onClick = {
                    scope.launch {
                        navigateToOldUi()
                    }
                }
            ) {
                Text("Back To Old UI", fontSize = 20.sp)
            }
            Button(
                onClick = {
                    scope.launch {
                        navigateToOldUi()
                    }
                }
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
