package com.jeeldobariya.passcodes.password_manager.presentation.view_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.core.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClassicalViewPasswordScreen(
    passwordId: Int,
    navigateTo: (Route) -> Unit,
    viewmodel: ViewPasswordViewModel = koinViewModel()
) {
    viewmodel.onAction(ViewPasswordAction.LoadPassword(passwordId))

    Scaffold { paddingValues ->
        val state by viewmodel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("View Password")

            Spacer(modifier = Modifier.padding(16.dp))

            Column(
                modifier = Modifier.padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Domain:       ${state.domain}", fontSize = 18.sp)
                Text("Username:     ${state.username}", fontSize = 18.sp)
                Text("Password:     ${state.password}", fontSize = 18.sp)
                Text("Notes:        ${state.notes}", fontSize = 18.sp)
                Text("LastUpdateAt: ${state.lastUpdatedAt}", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Button(
                onClick = {
                    navigateTo(Route.UpdatePassword(passwordId))
                }
            ) {
                Text("Update Password")
            }

            Button(
                onClick = {
                    viewmodel.onAction(ViewPasswordAction.DeletePasswordAction)
                    /* TODO: Navigate Back */
                }
            ) {
                Text("Delete Password")
            }

            Button(onClick = {
                /* TODO: Navigate Back */
            }) {
                Text(" Navigate Back ")
            }
        }
    }
}

@Preview
@Composable
fun ClassicalViewPasswordScreenPreview() {
    ClassicalViewPasswordScreen(passwordId = 0, navigateTo = {})
}
