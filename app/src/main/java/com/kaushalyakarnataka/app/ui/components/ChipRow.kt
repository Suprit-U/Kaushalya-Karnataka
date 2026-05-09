package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.ui.theme.*

@Composable
fun CategoryChipRow(
    categories: List<ServiceCategory>,
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "All" chip
        PremiumChip(
            label = "All",
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) }
        )

        categories.forEach { category ->
            PremiumChip(
                label = category.displayName,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun PremiumChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Primary else MaterialTheme.colorScheme.surface,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Text2,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chip_text"
    )
    val shadowElevation by animateColorAsState(
        targetValue = if (selected) Primary.copy(0.15f) else Color.Transparent,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chip_shadow"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .shadow(if (selected) 4.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = Primary.copy(0.1f))
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        tonalElevation = if (selected) 0.dp else 1.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
