package com.jeeldobariya.passcodes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme

@Composable
fun SettingsScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_passcodes),
                    contentDescription = "Passcodes Icon"
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.textview_settings_headline),
                    style = MaterialTheme.typography.displaySmall
                )

                Text(
                    text = stringResource(R.string.app_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Middle actions (primary content)
            Card(
                modifier = Modifier.fillMaxWidth(0.80f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.password_manager_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    FilledTonalButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.setting_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    FilledTonalButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.about_us_button_text), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@PreviewLightDark
@Composable
fun SettingsScreenPreview() {
    PasscodesTheme {
        SettingsScreen()
    }
}
