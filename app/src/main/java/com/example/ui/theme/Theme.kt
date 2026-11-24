package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Tokens Material 3 has no slot for: glass thickness, hairlines, hero gradient. */
@Immutable
data class BahiExtras(
    val isDark: Boolean,
    val paperHighlight: Color,
    val edge: Color,
    val shadowTint: Color,
    val ruleLine: Color,
    val marginLine: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val success: Color,
    val danger: Color,
)

private val LightExtras = BahiExtras(
    isDark = false,
    paperHighlight = Color.White,
    edge = Color(0x120F172A), // hairline: ink 7%
    shadowTint = InkDark,
    ruleLine = InkDark.copy(alpha = 0.045f),
    marginLine = DangerRed.copy(alpha = 0.22f),
    heroTop = Color(0xFF1A73E8),
    heroBottom = Color(0xFF4285F4),
    success = SuccessGreen,
    danger = DangerRed,
)

private val DarkExtras = BahiExtras(
    isDark = true,
    paperHighlight = Color.White.copy(alpha = 0.10f),
    edge = Color(0x14FFFFFF), // hairline: white 8%
    shadowTint = Color.Black,
    ruleLine = Color.White.copy(alpha = 0.035f),
    marginLine = Color(0xFFF87171).copy(alpha = 0.28f),
    heroTop = Color(0xFF2563EB),
    heroBottom = Color(0xFF4285F4),
    success = Color(0xFF52D394),
    danger = Color(0xFFF87171),
)

val LocalBahiExtras = staticCompositionLocalOf { LightExtras }

val MaterialTheme.bahi: BahiExtras
    @Composable @ReadOnlyComposable get() = LocalBahiExtras.current

// Gemini radii: cards 20–28dp, pills fully round, inputs 16dp, sheets 28dp top.
private val BahiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val DarkColorScheme = darkColorScheme(
    primary = GeminiBlueDark,
    onPrimary = Color(0xFF062144),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealContainerLight,
    secondary = NavActiveTextDark,
    onSecondary = InkDark,
    secondaryContainer = CyanContainerDark,
    onSecondaryContainer = CyanContainerLight,
    tertiary = GeminiLavenderDark,
    onTertiary = Color(0xFF1E2447),
    tertiaryContainer = Color(0xFF2A3160),
    onTertiaryContainer = AmberAccentLight,
    background = DarkBg,
    onBackground = GeminiTextPrimaryDark,
    surface = DarkSurface,
    onSurface = GeminiTextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = GeminiTextSecondaryDark,
    outline = DarkCardBorder,
    error = DangerRedDark,
    onError = Color(0xFF4A0A06),
)

private val LightColorScheme = lightColorScheme(
    primary = GeminiBlue,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = Color(0xFF0B2E5B),
    secondary = GeminiBlue2,
    onSecondary = Color.White,
    secondaryContainer = CyanContainerLight,
    onSecondaryContainer = InkDark,
    tertiary = GeminiLavender,
    onTertiary = Color.White,
    tertiaryContainer = AmberAccentLight,
    onTertiaryContainer = Color(0xFF1E2447),
    background = LightBg,
    onBackground = GeminiTextPrimary,
    surface = LightSurface,
    onSurface = GeminiTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = GeminiTextSecondary,
    outline = LightCardBorder,
    error = DangerRed,
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep the branded Gemini look; flip to true to follow wallpaper colours
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalBahiExtras provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BahiShapes,
            content = content,
        )
    }
}
