package com.kaushalyakarnataka.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// ============================================================
// KAUSHALYA-KARNATAKA SHAPE SYSTEM
// Mapped from CSS --r-* tokens:
//   --r-xs: 6px  → extraSmall
//   --r-sm: 10px → small
//   --r-md: 14px → medium (buttons, inputs)
//   --r-lg: 20px → large (cards)
//   --r-xl: 28px → extraLarge (bottom sheets, promo cards)
// ============================================================

val KaushalyaShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimens.radiusXs),   // 6dp — badges, tags
    small      = RoundedCornerShape(Dimens.radiusSm),   // 10dp — small buttons, chips
    medium     = RoundedCornerShape(Dimens.radiusMd),   // 14dp — buttons, inputs, modals
    large      = RoundedCornerShape(Dimens.radiusLg),   // 20dp — cards
    extraLarge = RoundedCornerShape(Dimens.radiusXl),   // 28dp — bottom sheets, promo
)
