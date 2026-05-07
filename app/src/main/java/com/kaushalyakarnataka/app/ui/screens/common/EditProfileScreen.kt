package com.kaushalyakarnataka.app.ui.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.AvatarComponent
import com.kaushalyakarnataka.app.ui.components.PrimaryButton
import com.kaushalyakarnataka.app.ui.components.PrimaryInputField
import com.kaushalyakarnataka.app.ui.screens.common.LoadingScreen
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    LaunchedEffect(userState) {
        if (userState is UiState.Success) {
            val user = (userState as UiState.Success).data
            name = user.name
            phone = user.phone
            location = user.location
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            onNavigateBack()
            viewModel.clearUpdateState()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Profile",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (val state = userState) {
            is UiState.Loading -> LoadingScreen(modifier = Modifier.padding(paddingValues))
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
            is UiState.Success -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarComponent(
                        imageUrl = state.data.avatarUrl,
                        name = state.data.name,
                        size = 100.dp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(onClick = { /* TODO: Change Photo */ }) {
                        Text("Change Photo")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryInputField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryInputField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryInputField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Location"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (updateState is UiState.Error) {
                        Text(
                            text = (updateState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    PrimaryButton(
                        text = "Save Changes",
                        onClick = { viewModel.updateProfile(name, phone, location) },
                        loading = updateState is UiState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
