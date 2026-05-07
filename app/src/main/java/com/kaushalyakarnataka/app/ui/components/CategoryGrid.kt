package com.kaushalyakarnataka.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.data.model.ServiceCategory

@Composable
fun CategoryGrid(
    categories: List<ServiceCategory>,
    onCategoryClick: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedCategories = categories.chunked(3)
        chunkedCategories.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowCategories.forEach { category ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryItem(
                            category = category,
                            onClick = { onCategoryClick(category) },
                        )
                    }
                }
                // Add empty boxes if the row is not full to keep alignment
                repeat(3 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconForCategory(category),
                contentDescription = category.displayName,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getIconForCategory(category: ServiceCategory): ImageVector {
    return when (category) {
        ServiceCategory.ELECTRICIAN -> Icons.Default.ElectricalServices
        ServiceCategory.PLUMBER -> Icons.Default.Plumbing
        ServiceCategory.CARPENTER -> Icons.Default.Carpenter
        ServiceCategory.PAINTER -> Icons.Default.FormatPaint
        ServiceCategory.CLEANER -> Icons.Default.CleaningServices
        ServiceCategory.AC_TECH -> Icons.Default.AcUnit
        ServiceCategory.GARDENER -> Icons.Default.Grass
        ServiceCategory.MECHANIC -> Icons.Default.Build
        ServiceCategory.OTHER -> Icons.Default.Build
    }
}
