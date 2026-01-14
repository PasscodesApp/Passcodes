package com.jeeldobariya.passcodes.password_manager.ui

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.jeeldobariya.passcodes.password_manager.presentation.save_password.SavePasswordAction
import com.jeeldobariya.passcodes.password_manager.presentation.save_password.SavePasswordViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavePasswordScreen(viewmodel: SavePasswordViewModel = koinViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        val state by viewmodel.state.collectAsState()

        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(value = state.domain,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeDomain(it))
                })
            OutlinedTextField(value = state.username,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeUsername(it))
                })
            OutlinedTextField(value = state.password,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangePassword(it))
                })
            OutlinedTextField(value = state.notes,
                onValueChange = {
                    viewmodel.onAction(action = SavePasswordAction.OnChangeNotes(it))
                })

            Button(onClick = {
                viewmodel.onAction(action = SavePasswordAction.OnSavePasswordButtonClick)
                scope.launch {
                    snackbarHostState.showSnackbar("Saved successfully")
                }
            }) {
                Text("Save Password")
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SavePasswordScreenPreview() {
    SavePasswordScreen()
}
