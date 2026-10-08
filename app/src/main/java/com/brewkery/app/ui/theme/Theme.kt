package com.brewkery.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BrewColors {
    val Background = Color(0xFFFFFAF7)
    val Dark = Color(0xFF140B07)
    val Accent = Color(0xFFD9532F)
    val AccentDark = Color(0xFF8E2E14)
    val AccentSoft = Color(0xFFF8E8DE)
    val Border = Color(0xFFEFDDD3)
    val BannerBg = Color(0xFFF8E7DC)
    val TextSecondary = Color(0xFF6F5F57)
    val BadgeBg = Color(0xFFFFF1C9)
    val BadgeText = Color(0xFF6B4208)
    val Green = Color(0xFF0B8F63)
    val Pink = Color(0xFFF0305C)
    val Amber = Color(0xFFFF9F0A)
    val Star = Color(0xFFF5B301)
}

@Composable
fun BrewkeryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BrewColors.Accent,
            onPrimary = Color.White,
            background = BrewColors.Background,
            onBackground = BrewColors.Dark,
            surface = Color.White,
            onSurface = BrewColors.Dark
        ),
        content = content
    )
}
