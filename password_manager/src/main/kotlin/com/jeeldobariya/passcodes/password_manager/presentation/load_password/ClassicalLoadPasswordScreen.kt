package com.jeeldobariya.passcodes.password_manager.presentation.load_password

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.core.navigation.Route
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ClassicalLoadPasswordScreen(
    navigateTo: (Route) -> Unit,
    viewmodel: LoadPasswordViewModel = koinViewModel()
) {
    viewmodel.onAction(LoadPasswordAction.RefreshPassword)
    val state by viewmodel.state.collectAsState()

    ClassicalLoadPasswordScreenContent(
        state = state,
        navigateTo = navigateTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicalLoadPasswordScreenContent(
    state: LoadPasswordState,
    navigateTo: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Load Passwords")
                }
            )
        }
    ) { paddingValue ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.passwordEntityList,
                key = { it.id }
            ) { passwordItem ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { navigateTo(Route.ViewPassword(passwordItem.id)) })
                        .padding(12.dp)
                ) {
                    Text(
                        text = passwordItem.domain,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = passwordItem.username,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }
    }
}

@Preview
@Composable
private fun ClassicalLoadPasswordScreenPreview() {
    ClassicalLoadPasswordScreenContent(
        state = LoadPasswordState(),
        navigateTo = {}
    )
}
