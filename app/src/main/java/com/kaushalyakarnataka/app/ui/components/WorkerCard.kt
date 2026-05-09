package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils

@Composable
fun WorkerCard(
    worker: Worker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "worker_card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(Dimens.radiusLg), spotColor = Primary.copy(0.08f))
            .clip(RoundedCornerShape(Dimens.radiusLg))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(Dimens.radiusLg),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box {
            // Subtle gradient accent on left edge
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryLight.copy(0.6f), Primary.copy(0.3f), PrimaryLight.copy(0.6f))
                        )
                    )
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarComponent(
                    imageUrl = worker.avatarUrl,
                    name = worker.name,
                    size = Dimens.avatarLg
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (worker.isVerified) {
                            VerifiedBadge()
                        }
                    }

                    Text(
                        text = worker.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Text3
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        if (worker.rating > 0 && worker.reviewCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Warning.copy(0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Warning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${String.format("%.1f", worker.rating)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Warning
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${worker.reviewCount} reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = Text4
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (worker.displayBaseCharge > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusSm))
                                    .background(PrimaryTint)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "~${CurrencyUtils.formatRupees(worker.displayBaseCharge)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        } else {
                            Text(
                                text = "Book Now",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }
        }
    }
}
