package com.jeeldobariya.passcodes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section
            Image(
                painter = painterResource(R.drawable.ic_passcodes),
                contentDescription = "Passcodes Icon",
                modifier = Modifier
                    .size(150.dp)
                    .padding(10.dp)
            )

            Text(
                text = stringResource(R.string.textview_settings_headline),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Coming Soon", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "Language & Theme features are currently under development",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.preview_layout), style = MaterialTheme.typography.bodyLarge)

                    Switch(
                        checked = true,
                        onCheckedChange = {

                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.latest_feature), style = MaterialTheme.typography.bodyLarge)

                    Switch(
                        checked = true,
                        onCheckedChange = {

                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.align(Alignment.TopStart),
                        onClick = { }
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "settings")
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = stringResource(R.string.autofill_settings))
                    }

                    OutlinedButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = { }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.Red)
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = stringResource(R.string.clear_all_data_button_text), color = Color.Red)
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
