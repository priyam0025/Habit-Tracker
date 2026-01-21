package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HitmakerColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = TextSecondary,
    background = Black,
    surface = SurfaceGrey,
    onPrimary = Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HitmakerColorScheme,
        typography = Typography,
        content = content
    )
}