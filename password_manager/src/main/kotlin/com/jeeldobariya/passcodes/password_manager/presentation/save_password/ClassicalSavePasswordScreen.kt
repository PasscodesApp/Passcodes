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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClassicalSavePasswordScreen(viewmodel: SavePasswordViewModel = koinViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        val state by viewmodel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Save Password")

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = state.domain,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeDomain(it))
                },
                label = {
                    Text("Domain:")
                },
                placeholder = {
                    Text("e.g. Google, Instagram etc...")
                }
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeUsername(it))
                },
                label = {
                    Text("Username:")
                },
                placeholder = {
                    Text("e.g. username / email / mobile.no.")
                }
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangePassword(it))
                },
                label = {
                    Text("Password:")
                },
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeNotes(it))
                },
                label = {
                    Text("Notes (Optional):")
                },
                placeholder = {
                    Text("e.g. Url or Platform Info. Account Info.")
                }
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                viewmodel.onAction(action = SavePasswordAction.OnSavePasswordButtonClick)
                scope.launch {
                    snackbarHostState.showSnackbar("Saved Successfully!!")
                }
            }) {
                Text("Save Password")
            }
        }
    }
}

@Preview
@Composable
fun ClassicalSavePasswordScreenPreview() {
    ClassicalSavePasswordScreen()
}
