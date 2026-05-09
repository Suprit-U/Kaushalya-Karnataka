package com.kaushalyakarnataka.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import com.kaushalyakarnataka.app.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.UserRole
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    role: UserRole,
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            onAuthSuccess()
        }
    }

    val isWorker = role == UserRole.WORKER
    val roleLabel = if (isWorker) "Worker" else "Customer"
    val headerColor1 = if (isWorker) Color(0xFF0F2055) else Primary
    val headerColor2 = if (isWorker) Color(0xFF1E3A8A) else PrimaryLight

    Column(modifier = modifier.fillMaxSize()) {
        // Premium animated gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(headerColor1, headerColor2)))
                .padding(bottom = 28.dp)
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(0.15f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    )
                }
                Spacer(Modifier.height(20.dp))
                AnimatedContent(
                    targetState = isLogin,
                    transitionSpec = {
                        (fadeIn(tween(250)) + slideInVertically { it / 4 }) togetherWith
                        (fadeOut(tween(150)) + slideOutVertically { it / 4 })
                    },
                    label = "header_title"
                ) { login ->
                    Column {
                        Text(
                            text = if (login) "Welcome back 👋" else "Create Account",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (login) "Sign in to your $roleLabel account" else "Join as a $roleLabel today",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                // Premium glass segmented control
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.12f))
                        .padding(4.dp)
                ) {
                    val items = listOf("Log In" to true, "Sign Up" to false)
                    val selectedIndex = if (isLogin) 0 else 1
                    // Sliding indicator
                    val indicatorOffset by animateDpAsState(
                        targetValue = if (selectedIndex == 0) 0.dp else (remember { mutableStateOf(0) }.value.let {
                            // Approximate half width minus padding
                            // Since we use weight-based, we'll overlay the active pill on the selected item
                            0.dp
                        }),
                        animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
                        label = "tab_indicator"
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        items.forEach { (label, login) ->
                            val selected = isLogin == login
                            val itemScale by animateFloatAsState(
                                targetValue = if (selected) 1.02f else 0.98f,
                                animationSpec = spring(stiffness = 500f),
                                label = "tab_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .scale(itemScale)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) Color.White else Color.Transparent)
                                    .clickable(onClick = { isLogin = login; viewModel.clearError() }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Primary else Color.White.copy(0.7f),
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Premium form body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(
                visible = !isLogin,
                enter = expandVertically(tween(300)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        singleLine = true
                    )
                    PremiumTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        singleLine = true
                    )
                }
            }

            PremiumTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                singleLine = true
            )

            PremiumTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                singleLine = true,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            // Premium error display
            val error = (authState as? UiState.Error)?.message
            AnimatedVisibility(
                visible = !error.isNullOrBlank(),
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(100))
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Error.copy(0.08f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Error.copy(0.2f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Error.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Premium gradient CTA button
            val isLoading = authState is UiState.Loading
            val btnScale by animateFloatAsState(
                targetValue = if (isLoading) 0.98f else 1f,
                animationSpec = spring(stiffness = 400f),
                label = "btn_scale"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(btnScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Primary, PrimaryLight)))
                    .clickable(enabled = !isLoading) {
                        viewModel.clearError()
                        if (isLogin) {
                            viewModel.loginWithRole(email.trim(), password, role)
                        } else {
                            viewModel.register(name, email.trim(), password, phone, role)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isLogin) Icons.AutoMirrored.Filled.Login else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (isLogin) "Login as $roleLabel" else "Create $roleLabel Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLogin) "Don't have an account?" else "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Text3
                )
                TextButton(onClick = { isLogin = !isLogin; viewModel.clearError() }) {
                    Text(
                        if (isLogin) "Sign Up" else "Login",
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    val isFocused = remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused.value) Primary else Border.copy(0.6f),
        animationSpec = tween(200),
        label = "field_border"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isFocused.value) Primary else Text3,
        animationSpec = tween(200),
        label = "field_label"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused.value) Primary.copy(0.03f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "field_bg"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor) },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused.value = it.isFocused },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(leadingIcon, null, tint = Primary.copy(0.7f), modifier = Modifier.size(18.dp))
            }
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password",
                        tint = Text3
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            focusedTextColor = Text1,
            unfocusedTextColor = Text1,
            cursorColor = Primary
        )
    )
}
