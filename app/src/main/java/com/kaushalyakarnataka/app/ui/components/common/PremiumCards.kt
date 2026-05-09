package com.kaushalyakarnataka.app.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.ui.theme.*

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.verticalGradient(listOf(Primary, PrimaryDark)),
    shape: Shape = RoundedCornerShape(Dimens.radiusLg),
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape, clip = false, spotColor = Primary.copy(0.15f))
            .clip(shape)
            .background(gradient)
            .padding(Dimens.sp4),
        content = content
    )
}

@Composable
fun ElevatedSurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.radiusLg),
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "card_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .scale(scale)
            .then(clickableModifier),
        shape = shape,
        tonalElevation = elevation,
        shadowElevation = elevation,
        color = MaterialTheme.colorScheme.surface,
        content = { Column(modifier = Modifier.padding(Dimens.sp4), content = content) }
    )
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.linearGradient(listOf(PrimaryTint, PrimarySubtle)),
    iconTint: Color = Primary
) {
    ElevatedSurfaceCard(modifier = modifier, elevation = 1.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(Dimens.radiusMd))
                .padding(Dimens.sp4)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(Dimens.sp3))
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Text1)
                Text(label, style = MaterialTheme.typography.labelMedium, color = Text3)
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(listOf(Primary, PrimaryLight)),
    icon: ImageVector? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(Dimens.buttonHeight)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Surface3, Surface3)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Icon(it, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) Color.White else Text4
            )
        }
    }
}

@Composable
fun PremiumOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "btn_out_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(Dimens.buttonHeight)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, Primary.copy(0.2f), RoundedCornerShape(Dimens.radiusMd))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Icon(it, null, tint = Primary, modifier = Modifier.size(20.dp)) }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Primary
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Dimens.sp8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Text4.copy(0.4f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.sp4))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = Text2, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(Dimens.sp2))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Text3)
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(Dimens.sp6))
            PremiumButton(text = actionText, onClick = onAction, modifier = Modifier.width(200.dp))
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusFull))
            .background(containerColor.copy(0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.radiusLg),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = { Column(modifier = Modifier.padding(Dimens.sp4), content = content) }
    )
}
