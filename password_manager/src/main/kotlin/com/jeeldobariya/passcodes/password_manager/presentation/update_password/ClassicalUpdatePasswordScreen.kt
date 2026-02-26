package com.jeeldobariya.passcodes.password_manager.presentation.update_password

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClassicalUpdatePasswordScreen(
    passwordId: Int,
    viewmodel: UpdatePasswordViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewmodel.loadInitialData(passwordId)

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
            Text("Update Password")

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = state.domain,
                onValueChange = {
                    viewmodel.onAction(action = UpdatePasswordAction.OnChangeDomain(it))
                },
                label = {
                    Text("Domain:")
                }
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = {
                    viewmodel.onAction(action = UpdatePasswordAction.OnChangeUsername(it))
                },
                label = {
                    Text("Username:")
                }
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    viewmodel.onAction(action = UpdatePasswordAction.OnChangePassword(it))
                },
                label = {
                    Text("Password:")
                }
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = {
                    viewmodel.onAction(action = UpdatePasswordAction.OnChangeNotes(it))
                },
                label = {
                    Text("Notes:")
                }
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                viewmodel.onAction(action = UpdatePasswordAction.OnUpdatePasswordButtonClick)
                scope.launch {
                    snackbarHostState.showSnackbar("Update Successfully!!")
                }
            }) {
                Text("Update Password")
            }
        }
    }
}


@Preview
@Composable
fun ClassicalUpdatePasswordScreenPreview() {
    ClassicalUpdatePasswordScreen(passwordId = 0)
}
