package com.jeeldobariya.passcodes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme

@Composable
fun MainScreen(navigateTo: (Route) -> Unit) {
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
                    text = stringResource(R.string.app_name),
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
                modifier = Modifier
                    .scale(1.25f)
                    .width(IntrinsicSize.Max),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navigateTo(Route.PasswordManager) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "lock")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = stringResource(R.string.password_manager_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    FilledTonalButton(
                        onClick = { navigateTo(Route.Settings) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "lock")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(stringResource(R.string.setting_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    FilledTonalButton(
                        onClick = { navigateTo(Route.AboutUs) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "lock")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(stringResource(R.string.about_us_button_text), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicalMainScreen(navigateTo: (Route) -> Unit) {
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
                    text = stringResource(R.string.app_name),
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
                modifier = Modifier
                    .scale(1.25f)
                    .width(IntrinsicSize.Max),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navigateTo(Route.PasswordManager) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "lock")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = stringResource(R.string.password_manager_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    OutlinedButton(
                        onClick = { navigateTo(Route.Settings) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "settings")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(stringResource(R.string.setting_button_text), style = MaterialTheme.typography.bodyLarge)
                    }

                    OutlinedButton(
                        onClick = { navigateTo(Route.AboutUs) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "info")
                        Spacer(modifier = Modifier.padding(4.dp))
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
fun MainScreenPreview() {
    PasscodesTheme {
        MainScreen(navigateTo = { })
    }
}
