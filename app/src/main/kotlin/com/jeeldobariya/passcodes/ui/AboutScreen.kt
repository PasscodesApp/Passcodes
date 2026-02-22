package com.jeeldobariya.passcodes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
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


@Composable
fun ClassicalAboutScreen() {

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

