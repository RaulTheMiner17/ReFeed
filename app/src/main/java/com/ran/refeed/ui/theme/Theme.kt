package com.ran.refeed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ran.refeed.R

val Green = Color(0xFF50944A)
val LightGreen = Color(0xFF64CE4A)
val White = Color(0xFFFFFEE1)

// Define BeVietnamPro font family with correct file names
val BeVietnamProFamily = FontFamily(
    Font(R.font.bevietnam_pro_regular, FontWeight.Normal),
    Font(R.font.bevietnam_pro_medium, FontWeight.Medium),
    Font(R.font.bevietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.bevietnam_pro_bold, FontWeight.Bold),
    // Add more weights if needed
)

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