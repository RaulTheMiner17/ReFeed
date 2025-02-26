// ui/theme/Theme.kt
package com.ran.refeed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green = Color(0xFF4CAF50)
val LightGreen = Color(0xFFAED581)
val White = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    secondary = LightGreen,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun ReFeedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}