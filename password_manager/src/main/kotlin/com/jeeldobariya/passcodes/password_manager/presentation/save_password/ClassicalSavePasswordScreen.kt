package com.jeeldobariya.passcodes.password_manager.presentation.save_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import com.jeeldobariya.passcodes.password_manager.R
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClassicalSavePasswordScreen(viewmodel: SavePasswordViewModel = koinViewModel()) {
    val state by viewmodel.state.collectAsState()

    ClassicalSavePasswordScreenContent(state = state, onAction = viewmodel::onAction)
}

@Composable
private fun ClassicalSavePasswordScreenContent(
    state: SavePasswordState,
    onAction: (SavePasswordAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.textview_savepassword_headline))

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = state.domain,
                onValueChange = {
                    onAction(SavePasswordAction.OnChangeDomain(it))
                },
                label = {
                    Text(stringResource(R.string.domain_prefix))
                },
                placeholder = {
                    Text("e.g. Google, Instagram etc...")
                }
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = {
                    onAction(SavePasswordAction.OnChangeUsername(it))
                },
                label = {
                    Text(stringResource(R.string.username_prefix))
                },
                placeholder = {
                    Text("e.g. username / email / mobile.no.")
                }
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    onAction(SavePasswordAction.OnChangePassword(it))
                },
                label = {
                    Text(stringResource(R.string.password_prefix))
                },
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = {
                    onAction(SavePasswordAction.OnChangeNotes(it))
                },
                label = {
                    Text("Notes (Optional):")
                },
                placeholder = {
                    Text("e.g. Url / Platform Info / Account Info.")
                }
            )

            OutlinedTextField(
                value = state.url,
                onValueChange = {
                    onAction(SavePasswordAction.OnChangeUrl(it))
                },
                label = {
                    Text("URL (Optional):")
                },
                placeholder = {
                    Text("e.g. https://github.com/login, https://accounts.google.com")
                }
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                onAction(SavePasswordAction.OnSavePasswordButtonClick)
                scope.launch {
                    snackbarHostState.showSnackbar("Saved Successfully!!")
                }
            }) {
                Text(stringResource(R.string.save_password_button_text))
            }
        }
    }
}

@Preview
@Composable
private fun ClassicalSavePasswordScreenPreview() {
    PasscodesTheme {
        ClassicalSavePasswordScreenContent(
            state = SavePasswordState(),
            onAction = {}
        )
    }
}
