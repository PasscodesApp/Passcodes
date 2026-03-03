package com.jeeldobariya.passcodes.presentation.setting_screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.autofill.AutofillSettingsActivity
import com.jeeldobariya.passcodes.core.datastore.AppSettings
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme
import kotlinx.coroutines.launch


@Composable
fun ClassicalSettingsScreen() {
    ClassicalSettingsScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicalSettingsScreenContent() {
    // TODO: Language & Theme need to be done.

    val selectedLanguage = "Under Development"
    val languageOptions: List<String> = listOf("English", "Korean")
    val onLanguageSelected: (String) -> Unit = { /* TODO */ }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val flagDataStore = context.featureFlagsDatastore
    val flagDatastoreState by flagDataStore.data.collectAsState(
        FeatureFlagsSettings(
            version = 0,
            isPreviewFeaturesEnabled = false,
            isPreviewLayoutEnabled = false
        )
    )
    val appDataStore = context.appDatastore
    val appDatastoreState by appDataStore.data.collectAsState(initial = AppSettings())

    var expanded by remember { mutableStateOf(false) }

    Scaffold { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Headline
            item {
                Text(
                    text = stringResource(R.string.textview_settings_headline),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }

            // Language Card
            item {
                Card(shape = RoundedCornerShape(16.dp)) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = stringResource(R.string.label_language_setting),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedLanguage,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier.menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                languageOptions.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            onLanguageSelected(lang)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Theme Card
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = stringResource(R.string.label_theme_setting),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(onClick = { /* TODO */ }) {
                            Text(
                                text = stringResource(R.string.toggle_theme_button_text),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Latest Features Switch
            item {
                SwitchCard(
                    text = stringResource(R.string.latest_feature),
                    checked = flagDatastoreState.isPreviewFeaturesEnabled,
                    onCheckedChange = {
                        scope.launch {
                            flagDataStore.updateData {
                                it.copy(isPreviewFeaturesEnabled = !it.isPreviewFeaturesEnabled)
                            }
                        }
                    }
                )
            }

            // Latest Layout Switch
            item {
                SwitchCard(
                    text = stringResource(R.string.preview_layout),
                    checked = flagDatastoreState.isPreviewLayoutEnabled,
                    onCheckedChange = {
                        scope.launch {
                            flagDataStore.updateData {
                                it.copy(isPreviewLayoutEnabled = !it.isPreviewLayoutEnabled)
                            }
                        }
                    }
                )
            }

            // Modern Layout Switch
            item {
                SwitchCard(
                    text = "Modern Layout",
                    checked = appDatastoreState.isModernLayoutEnable,
                    onCheckedChange = {
                        scope.launch {
                            appDataStore.updateData {
                                it.copy(isModernLayoutEnable = !it.isModernLayoutEnable)
                            }
                        }
                    },
                    enabled = flagDatastoreState.isPreviewLayoutEnabled
                )
            }

            // Autofill Settings
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(onClick = {
                            Intent(context, AutofillSettingsActivity::class.java).also {
                                context.startActivity(it)
                            }
                        }) {
                            Text(
                                text = stringResource(R.string.autofill_settings),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Clear All Data
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO */ },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.clear_all_data_button_text))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchCard(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(shape = RoundedCornerShape(16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Preview
@Composable
private fun ClassicalSettingsScreenPreview() {
    PasscodesTheme {
        ClassicalSettingsScreenContent()
    }
}
