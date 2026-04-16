package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import com.jeeldobariya.passcodes.password_manager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicalPasswordManagerScreen(navigateTo: (Route) -> Unit) {
    ClassicalPasswordManagerScreenContent(navigateTo)
}

@Composable
private fun ClassicalPasswordManagerScreenContent(
    navigateTo: (Route) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.textview_passwordmanager_headline), fontSize = 36.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                navigateTo(Route.SavePassword)
            }) {
                Text(stringResource(R.string.save_password_button_text))
            }

            Button(onClick = {
                navigateTo(Route.LoadPassword)
            }) {
                Text(stringResource(R.string.load_password_button_text))
            }
        }
    }
}

@Preview
@Composable
private fun ClassicalPasswordManagerScreenPreview() {
    PasscodesTheme {
        ClassicalPasswordManagerScreenContent(navigateTo = {})
    }
}
