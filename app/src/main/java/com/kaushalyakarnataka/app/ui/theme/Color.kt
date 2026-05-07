package com.kaushalyakarnataka.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// KAUSHALYA-KARNATAKA DESIGN SYSTEM — COLOR TOKENS
// Translated 1:1 from CSS :root variables in Design-Prototype.html
// Primary: Deep Blue | Secondary: Saffron Orange | Material 3
// ============================================================

// --- Primary (Deep Blue) ---
val Primary         = Color(0xFF1E3A8A)
val PrimaryDark     = Color(0xFF162B6B)
val PrimaryLight    = Color(0xFF2563EB)
val PrimaryTint     = Color(0xFFEFF6FF)
val PrimarySubtle   = Color(0xFFDBEAFE)

// --- Secondary (Saffron Orange) ---
val Secondary       = Color(0xFFF97316)
val SecondaryDark   = Color(0xFFEA580C)
val SecondaryTint   = Color(0xFFFFF7ED)
val SecondarySubtle = Color(0xFFFFEDD5)

// --- Semantic Colors ---
val Success         = Color(0xFF16A34A)
val SuccessTint     = Color(0xFFDCFCE7)
val Warning         = Color(0xFFD97706)
val WarningTint     = Color(0xFFFEF3C7)
val Error           = Color(0xFFDC2626)
val ErrorTint       = Color(0xFFFEE2E2)

// --- Neutral Background & Surface ---
val Background      = Color(0xFFF8FAFC)
val Surface         = Color(0xFFFFFFFF)
val Surface2        = Color(0xFFF1F5F9)
val Surface3        = Color(0xFFE2E8F0)
val Border          = Color(0xFFE2E8F0)
val BorderStrong    = Color(0xFFCBD5E1)

// --- Text Scale ---
val Text1           = Color(0xFF0F172A)  // Strongest — headings
val Text2           = Color(0xFF334155)  // Body text
val Text3           = Color(0xFF64748B)  // Subtext / captions
val Text4           = Color(0xFF94A3B8)  // Placeholder / muted
val TextInverse     = Color(0xFFFFFFFF)  // On dark backgrounds

// --- Welcome screen gradient colors ---
val WelcomeGradientStart  = Color(0xFF0F2055)
val WelcomeGradientMiddle = Color(0xFF1E3A8A)
val WelcomeGradientEnd    = Color(0xFF1D4ED8)

// --- Star / Rating color ---
val StarColor       = Color(0xFFFCD34D)

// --- Material3 scheme overrides ---
val md_theme_light_primary              = Primary
val md_theme_light_onPrimary            = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer     = PrimaryTint
val md_theme_light_onPrimaryContainer   = PrimaryDark
val md_theme_light_secondary            = Secondary
val md_theme_light_onSecondary          = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer   = SecondarySubtle
val md_theme_light_onSecondaryContainer = SecondaryDark
val md_theme_light_background           = Background
val md_theme_light_onBackground         = Text1
val md_theme_light_surface              = Surface
val md_theme_light_onSurface            = Text1
val md_theme_light_surfaceVariant       = Surface2
val md_theme_light_onSurfaceVariant     = Text3
val md_theme_light_error                = Error
val md_theme_light_onError              = Color(0xFFFFFFFF)
val md_theme_light_errorContainer       = ErrorTint
val md_theme_light_onErrorContainer     = Color(0xFF410002)
val md_theme_light_outline              = Border
val md_theme_light_outlineVariant       = BorderStrong
val md_theme_light_scrim               = Color(0xFF000000)
