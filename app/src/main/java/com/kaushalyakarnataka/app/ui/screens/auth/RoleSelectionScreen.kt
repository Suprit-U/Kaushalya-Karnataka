package com.kaushalyakarnataka.app.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.data.model.UserRole
import com.kaushalyakarnataka.app.ui.theme.*

@Composable
fun RoleSelectionScreen(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Premium gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 28.dp)
        ) {
            Column {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(0.15f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Choose Your Role",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "How do you want to use the app?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(0.85f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RoleCard(
                title = "I'm a Customer",
                description = "Hire skilled local workers for your needs",
                icon = Icons.Default.Person,
                isSelected = selectedRole == UserRole.CUSTOMER,
                onClick = { onRoleSelected(UserRole.CUSTOMER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleCard(
                title = "I'm a Kaushal (Worker)",
                description = "Offer your services and find new jobs",
                icon = Icons.Default.Engineering,
                isSelected = selectedRole == UserRole.WORKER,
                onClick = { onRoleSelected(UserRole.WORKER) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Premium gradient CTA
            val canContinue = selectedRole != null
            val btnAlpha by animateFloatAsState(
                targetValue = if (canContinue) 1f else 0.5f,
                animationSpec = tween(200),
                label = "btn_alpha"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canContinue) Brush.horizontalGradient(listOf(Primary, PrimaryLight))
                        else Brush.horizontalGradient(listOf(Primary.copy(0.5f), PrimaryLight.copy(0.5f)))
                    )
                    .clickable(enabled = canContinue, onClick = onContinueClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(btnAlpha)
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.6f),
        label = "role_card_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Primary else Border,
        animationSpec = tween(250),
        label = "role_border"
    )
    val iconBgColor = if (isSelected) Primary.copy(0.12f) else Primary.copy(0.06f)
    val iconTint = if (isSelected) Primary else Text3
    val titleColor = if (isSelected) Primary else Text1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(20.dp), spotColor = Primary.copy(0.06f))
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Text3
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
