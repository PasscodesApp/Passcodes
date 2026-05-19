package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.design_system.theme.PasscodesTheme
import com.jeeldobariya.passcodes.password_manager.R
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicalPasswordManagerScreen(navigateTo: (Route) -> Unit, viewModel: PasswordManagerViewModel = koinViewModel()) {
    val context = LocalContext.current

    // 1. Setup the Import File Picker Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        requireNotNull(uri)

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            viewModel.onAction(PasswordManagerAction.OnImportGooglePassword(uri))
        } else {
            Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Setup the Export Document Picker Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        requireNotNull(uri)

        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()
            viewModel.onAction(PasswordManagerAction.OnExportGooglePassword(uri))
        } else {
            Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    ClassicalPasswordManagerScreenContent(
        navigateTo = navigateTo,
        onImportClicked = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("text/comma-separated-values")
                putExtra(Intent.EXTRA_TITLE, "passwords.csv")
            }

            importLauncher.launch(intent)
        },
        onExportClicked = {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType("text/comma-separated-values")
                putExtra(Intent.EXTRA_TITLE, "passwords.csv")
            }

            exportLauncher.launch(intent)
        }
    )
}

@Composable
private fun ClassicalPasswordManagerScreenContent(
    navigateTo: (Route) -> Unit,
    onImportClicked: () -> Unit,
    onExportClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.textview_passwordmanager_headline), fontSize = 36.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                navigateTo(Route.SavePassword)
            }) {
                Text(stringResource(R.string.save_password_button_text))
            }

            Button(onClick = {
                navigateTo(Route.LoadPassword)
            }) {
                Text(stringResource(R.string.load_password_button_text))
            }

            OutlinedButton(onClick = {
                onImportClicked()
            }) {
                Text(stringResource(R.string.import_password_button_text))
            }
            OutlinedButton(onClick = {
                onExportClicked()
            }) {
                Text(stringResource(R.string.export_password_button_text))
            }
        }
    }
}

@Preview
@Composable
private fun ClassicalPasswordManagerScreenPreview() {
    PasscodesTheme {
        ClassicalPasswordManagerScreenContent(navigateTo = {}, {}, {})
    }
}
