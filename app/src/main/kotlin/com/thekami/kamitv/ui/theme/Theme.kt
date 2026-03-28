package com.thekami.kamitv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KamiTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background     = Background,
            surface        = Surface,
            surfaceVariant = SurfaceVariant,
            primary        = Primary,
            onBackground   = OnBackground,
            onSurface      = OnSurface,
            onPrimary      = Background,
            outline        = Outline,
        ),
        content = content,
    )
}
