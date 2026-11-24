package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  "Gemini" palette — frost white fading into airy sky blue,
//  glass surfaces, Google blue accent, lavender, gradient money.
//  Source: the user's helpformuse reference prototype
//  (gemini_style_fish_sales_app_redesign.html).
//
//  Every identifier from the old palette is kept so existing
//  screens re-skin without edits; only the values changed.
// ─────────────────────────────────────────────────────────────

// Primary: Gemini blue (was tide teal)
val TealPrimary = Color(0xFF1A73E8)
val TealPrimaryDark = Color(0xFF1765CC)
val TealContainerLight = Color(0xFFDCE9FD)
val TealContainerDark = Color(0xFF1E3A5F)

// Accent: Lavender (was brass)
val AmberAccent = Color(0xFF6B7DE0)
val AmberAccentLight = Color(0xFFE7EAFD)
val AmberAccentDark = Color(0xFF4A5BC0)

// Secondary: Google blue
val CyanSecondary = Color(0xFF4285F4)
val CyanSecondaryDark = Color(0xFF7CB3FF)
val CyanContainerLight = Color(0xFFE3EEFD)
val CyanContainerDark = Color(0xFF1C2F4A)

// Dark surfaces: AMOLED black + blue glow
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0C101A)
val DarkSurfaceVariant = Color(0xFF151B28)
val DarkCardBorder = Color(0x14FFFFFF) // white 8%

// Light surfaces: frost white → sky blue mist
val LightBg = Color(0xFFF8FAFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE8F2FF)
val LightCardBorder = Color(0x120F172A) // ink 7%

// Ink
val InkDark = Color(0xFF0F172A)

// Status & metric colours
val SuccessGreen = Color(0xFF1F9D55)
val WarningOrange = Color(0xFFD9822B)
val DangerRed = Color(0xFFDC2626) // dues, deletions
val GoogleBlue = Color(0xFF4285F4)
val DriveGreen = Color(0xFF0F9D58)
val CloudPurple = Color(0xFF8B5CF6)

// ── Gemini tokens (from the reference prototype) ─────────────
val GeminiBlue = Color(0xFF1A73E8)
val GeminiBlue2 = Color(0xFF4285F4)
val GeminiBlueLight = Color(0xFF5E97F6)
val GeminiLavender = Color(0xFF6B7DE0)
val GeminiLavenderLight = Color(0xFF7B8CDE)
val GeminiSky = Color(0xFF38BDF8)

// FAB gradient: 135deg #1A73E8 → #4285F4 → #6B88F7
val FabGradientStart = Color(0xFF1A73E8)
val FabGradientMid = Color(0xFF4285F4)
val FabGradientEnd = Color(0xFF6B88F7)
// FAB gradient (dark): #2563EB → #4285F4 → #7C8DF8
val FabGradientStartDark = Color(0xFF2563EB)
val FabGradientEndDark = Color(0xFF7C8DF8)

// Gradient money text: 95deg #1A73E8 → #4285F4 → #7B8CDE
val MoneyGradientStart = Color(0xFF1A73E8)
val MoneyGradientMid = Color(0xFF4285F4)
val MoneyGradientEnd = Color(0xFF7B8CDE)
// Gradient money text (dark): #60A5FA → #818CF8 → #A5B4FC
val MoneyGradientStartDark = Color(0xFF60A5FA)
val MoneyGradientMidDark = Color(0xFF818CF8)
val MoneyGradientEndDark = Color(0xFFA5B4FC)

// Accent for dark mode
val GeminiBlueDark = Color(0xFF60A5FA)
val GeminiLavenderDark = Color(0xFF8DA2FB)
val NavActiveTextDark = Color(0xFF7CB3FF)

// Text colours (light)
val GeminiTextPrimary = Color(0xFF0F172A)
val GeminiTextSecondary = Color(0xFF52627A)
val GeminiTextMuted = Color(0xFF7C8CA3)
// Text colours (dark)
val GeminiTextPrimaryDark = Color(0xFFF1F5F9)
val GeminiTextSecondaryDark = Color(0xFF94A3B8)
val GeminiTextMutedDark = Color(0xFF64748B)

// Bottom nav
val NavInactiveLight = Color(0xFF5A6D86)
val NavInactiveDark = Color(0xFF7C8DA6)

// Danger (dark)
val DangerRedDark = Color(0xFFF87171)
