package com.jeeldobariya.passcodes.password_manager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.LoadPasswordAction
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.LoadPasswordViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(navigateTo: (Route) -> Unit, viewmodel: LoadPasswordViewModel = koinViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Password Manager") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Coming Soon")
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateTo(Route.SavePassword) }) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "")
            }
        }
    ) { paddingValue ->
        viewmodel.onAction(LoadPasswordAction.RefreshPassword)
        val state = viewmodel.state.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValue),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.value.passwordEntityList,
                key = { it.id }
            ) { passwordItem ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { navigateTo(Route.UpdatePassword(passwordItem.id)) })
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

@PreviewLightDark
@Composable
fun PasswordManagerScreenPreview() {
    PasswordManagerScreen({ })
}
