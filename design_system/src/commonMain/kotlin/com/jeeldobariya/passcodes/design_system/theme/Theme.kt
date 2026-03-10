package com.jeeldobariya.passcodes.design_system.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PasscodesColorScheme = ColorTheme(
    lightColorScheme = lightColorScheme(
        primary = primaryLight,
        secondary = secondaryLight,
        tertiary = tertiaryLight,
        background = BackgroundLight,
        surface = SurfaceLight,
        error = ErrorLight
    ),
    darkColorScheme = darkColorScheme(
        primary = primaryDark,
        secondary = secondaryDark,
        tertiary = tertiaryDark,
        background = BackgroundDark,
        surface = SurfaceDark,
        error = ErrorDark
    )
)

@Composable
fun PasscodesTheme(
    colorTheme: ColorTheme = PasscodesColorScheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> colorTheme.darkColorScheme
        else -> colorTheme.lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
