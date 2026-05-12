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

// --- Dark Mode Tokens (Premium AMOLED-friendly with rich tonal depth) ---
val DarkBackground              = Color(0xFF02040A)  // Deepest black with micro blue tint
val DarkOnBackground            = Color(0xFFF1F5F9)
val DarkSurface                 = Color(0xFF0B1221)  // Rich navy surface — not flat gray
val DarkOnSurface               = Color(0xFFF8FAFC)
val DarkSurfaceVariant          = Color(0xFF141D2E)  // Elevated card surface
val DarkOnSurfaceVariant        = Color(0xFFB8C5D6)
val DarkSurfaceDim              = Color(0xFF070D18)
val DarkSurfaceBright           = Color(0xFF1A2744)
val DarkSurfaceContainer        = Color(0xFF111B2E)
val DarkSurfaceContainerHigh    = Color(0xFF16233A)
val DarkSurfaceContainerHighest = Color(0xFF1C2D48)
val DarkOutline                 = Color(0xFF2A3A52)
val DarkOutlineVariant          = Color(0xFF1A2538)
val DarkInverseSurface          = Color(0xFFE2E8F0)
val DarkInverseOnSurface        = Color(0xFF0F172A)
val DarkInversePrimary          = Color(0xFF1E3A8A)
val DarkPrimaryTint             = Color(0xFF1E3A8A).copy(0.25f)
val DarkCardBorder              = Color(0xFF1E3A5F).copy(0.4f)

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
