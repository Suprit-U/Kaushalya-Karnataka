package com.kaushalyakarnataka.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.kaushalyakarnataka.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.ui.theme.*

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_float")

    // Floating orbs animation
    val orb1Offset by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb1"
    )
    val orb2Offset by infiniteTransition.animateFloat(
        initialValue = 15f, targetValue = -15f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb2"
    )
    val orb3Offset by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 25f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb3"
    )
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logo_pulse"
    )

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Primary.copy(0.08f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0.3f, 0.2f),
                        radius = 800f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(PrimaryLight.copy(0.06f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0.8f, 0.7f),
                        radius = 700f
                    )
                )
        )

        // Floating decorative orbs
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-30).dp, y = (80 + orb1Offset).dp)
                .clip(CircleShape)
                .background(Primary.copy(0.04f))
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (20).dp, y = (200 + orb2Offset).dp)
                .clip(CircleShape)
                .background(Secondary.copy(0.05f))
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = (-60 + orb3Offset).dp)
                .clip(CircleShape)
                .background(Primary.copy(0.03f))
                .align(Alignment.BottomEnd)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo image container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Primary.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Kaushalya Karnataka Logo",
                    modifier = Modifier.size(100.dp).scale(logoScale)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Kaushalya",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Text1,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Karnataka",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Find skilled local workers or offer your services to the community.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Text3,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Premium gradient CTA button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Primary, PrimaryLight)))
                    .clickable(onClick = onGetStartedClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
