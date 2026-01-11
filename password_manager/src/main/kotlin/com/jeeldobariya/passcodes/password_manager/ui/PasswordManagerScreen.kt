package com.jeeldobariya.passcodes.password_manager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Manager") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "")
            }
        }
    ) { paddingValue ->
        LazyColumn(modifier = Modifier.padding(paddingValue)) {
            (1..10).forEach {
                item {
                    Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                        Text("Domain $it", style = MaterialTheme.typography.titleLarge)
                        Text("Username $it", style = MaterialTheme.typography.bodySmall)
                    }

                    HorizontalDivider(thickness = 2.dp)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PasswordManagerScreenPreview() {
    PasswordManagerScreen()
}
