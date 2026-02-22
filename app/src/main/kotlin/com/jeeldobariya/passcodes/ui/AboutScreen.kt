package com.jeeldobariya.passcodes.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.jeeldobariya.passcodes.Constant
import com.jeeldobariya.passcodes.R
import com.jeeldobariya.passcodes.ui.ui.theme.PasscodesTheme

@Composable
fun ModernAboutScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 64.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_passcodes),
                contentDescription = "Passcodes Icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(10.dp)
            )

            Text(
                text = stringResource(R.string.textview_aboutus_headline),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.textview_app_description),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = stringResource(R.string.textview_app_warning),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red
            )

            Spacer(modifier = Modifier.padding(16.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicalAboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.textview_app_description),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.textview_app_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Grid Section
            item {
                AboutGridSection(
                    {},
                    {},
                    {},
                    {},
                    {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.label_contributor),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                ContributorCard(stringResource(R.string.developer_name))
            }

            item {
                ContributorCard(stringResource(R.string.code_maintainer))
            }

            item {
                ContributorCard(stringResource(R.string.co_developer))
            }
        }
    }
}

@Composable
private fun AboutGridSection(
    onSecurityClick: () -> Unit,
    onReleaseNotesClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onReportBugClick: () -> Unit,
    onTelegramClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AboutCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_security),
                text = stringResource(R.string.view_security_guidelines_button_text),
                onClick = {
                    Intent(Intent.ACTION_VIEW, Constant.SECURITY_GUIDE_URL.toUri()).also {
                        context.startActivity(it)
                    }
                }
            )

            AboutCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_history),
                text = stringResource(R.string.view_release_notes_button_text),
                onClick = {
                    Intent(Intent.ACTION_VIEW, Constant.RELEASE_NOTE_URL.toUri()).also {
                        context.startActivity(it)
                    }
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AboutCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_article),
                text = stringResource(R.string.view_license_button_text),
                onClick = {
                    /* TODO: Add License Activity Link */

                    Intent(
                        Intent.ACTION_VIEW,
                        "https://passcodesapp.github.io/Passcodes-Docs/LICENSE/".toUri()
                    ).also {
                        context.startActivity(it)
                    }
                }
            )

            AboutCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_bug_report),
                text = stringResource(R.string.view_report_bug_text),
                onClick = {
                    Intent(Intent.ACTION_VIEW, Constant.REPORT_BUG_URL.toUri()).also {
                        context.startActivity(it)
                    }
                }
            )
        }

        TelegramCard(
            text = stringResource(R.string.view_telegram_community_text),
            onClick = {
                Intent(Intent.ACTION_VIEW, Constant.TELEGRAM_COMMUNITY_URL.toUri()).also {
                    context.startActivity(it)
                }
            }
        )
    }
}


@Composable
private fun AboutCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = icon,
                contentDescription = text,
                modifier = Modifier
                    .size(32.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TelegramCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = text,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ContributorCard(text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@PreviewLightDark
@Composable
fun ModernAboutScreenPreview() {
    PasscodesTheme {
        ModernAboutScreen()
    }
}

@PreviewLightDark
@Composable
fun ClassicalAboutScreenPreview() {
    PasscodesTheme {
        ClassicalAboutScreen()
    }
}
