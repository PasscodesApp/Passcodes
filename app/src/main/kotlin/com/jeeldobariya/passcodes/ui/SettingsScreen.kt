package com.jeeldobariya.passcodes.ui

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.autofill.AutofillSettingsActivity
import com.jeeldobariya.passcodes.core.datastore.AppSettings
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun ModernSettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val flagDataStore = LocalContext.current.featureFlagsDatastore
    val flagDatastoreState by flagDataStore.data.collectAsState(
        FeatureFlagsSettings(
            version = 0,
            isPreviewFeaturesEnabled = false,
            isPreviewLayoutEnabled = false
        )
    )
    val appDataStore = LocalContext.current.appDatastore
    val appDatastoreState by appDataStore.data.collectAsState(initial = AppSettings())

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section
            Image(
                painter = painterResource(R.drawable.ic_passcodes),
                contentDescription = "Passcodes Icon",
                modifier = Modifier
                    .size(150.dp)
                    .padding(10.dp)
            )

            Text(
                text = stringResource(R.string.textview_settings_headline),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Coming Soon", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "Language & Theme features are currently under development",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val activity = LocalActivity.current

                    Text(text = stringResource(R.string.preview_layout), style = MaterialTheme.typography.bodyLarge)

                    Switch(
                        checked = flagDatastoreState.isPreviewLayoutEnabled,
                        onCheckedChange = {
                            scope.launch {
                                flagDataStore.updateData {
                                    it.copy(isPreviewLayoutEnabled = !it.isPreviewLayoutEnabled)
                                }

                                activity?.finishAndRemoveTask()
                                exitProcess(0)
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.latest_feature), style = MaterialTheme.typography.bodyLarge)

                    Switch(
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

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Modern Layout", style = MaterialTheme.typography.bodyLarge)

                    Switch(
                        enabled = flagDatastoreState.isPreviewLayoutEnabled,
                        checked = appDatastoreState.isModernLayoutEnable,
                        onCheckedChange = {
                            scope.launch {
                                appDataStore.updateData {
                                    it.copy(isModernLayoutEnable = !it.isModernLayoutEnable)
                                }
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 2.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    OutlinedButton(modifier = Modifier.align(Alignment.TopStart), onClick = {
                        Intent(context, AutofillSettingsActivity::class.java).also {
                            context.startActivity(it)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "settings")
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = stringResource(R.string.autofill_settings))
                    }

                    OutlinedButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = { }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.Red)
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = stringResource(R.string.clear_all_data_button_text), color = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicalSettingsScreen(
    selectedLanguage: String,
    languageOptions: List<String>,
    onLanguageSelected: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onClearAllDataClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val flagDataStore = LocalContext.current.featureFlagsDatastore
    val flagDatastoreState by flagDataStore.data.collectAsState(
        FeatureFlagsSettings(
            version = 0,
            isPreviewFeaturesEnabled = false,
            isPreviewLayoutEnabled = false
        )
    )
    val appDataStore = LocalContext.current.appDatastore
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

                        Button(onClick = onToggleTheme) {
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
                val activity = LocalActivity.current

                SwitchCard(
                    text = stringResource(R.string.preview_layout),
                    checked = flagDatastoreState.isPreviewLayoutEnabled,
                    onCheckedChange = {
                        scope.launch {
                            flagDataStore.updateData {
                                it.copy(isPreviewLayoutEnabled = !it.isPreviewLayoutEnabled)
                            }

                            activity?.finishAndRemoveTask()
                            exitProcess(0)
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
                            onClick = onClearAllDataClick,
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
fun SwitchCard(
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

@PreviewLightDark
@Composable
fun ModernSettingsScreenPreview() {
    PasscodesTheme {
        ModernSettingsScreen()
    }
}

@PreviewLightDark
@Composable
fun ClassicalSettingsScreenPreview() {
    PasscodesTheme {
        ClassicalSettingsScreen(
            selectedLanguage = "korean",
            languageOptions = listOf("eng", "korean"),
            onLanguageSelected = {},
            onToggleTheme = {},
            onClearAllDataClick = {}
        )
    }
}
