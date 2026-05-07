package com.kaushalyakarnataka.app.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Extension to add a click listener to a Compose Modifier without showing the ripple effect.
 * Useful for custom components where you want to handle the ripple manually, or for
 * clickable areas that shouldn't show visual feedback (like closing a modal by clicking outside).
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}
