package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.PricingType
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.ServiceDuration
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.PrimaryButton
import com.kaushalyakarnataka.app.ui.components.PrimaryInputField
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.ServiceViewModel

@Composable
fun AddServiceScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    
    val addState by viewModel.addServiceState.collectAsState()

    LaunchedEffect(addState) {
        if (addState is UiState.Success) {
            viewModel.clearState()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Service",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            PrimaryInputField(
                value = name,
                onValueChange = { name = it },
                label = "Service Name"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryInputField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryInputField(
                value = priceStr,
                onValueChange = { priceStr = it.filter { char -> char.isDigit() } },
                label = "Starting Price (₹)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (addState is UiState.Error) {
                Text(
                    text = (addState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            PrimaryButton(
                text = "Save Service",
                onClick = {
                    val price = priceStr.toIntOrNull() ?: 0
                    if (name.isNotBlank() && description.isNotBlank() && price > 0) {
                        viewModel.addService(
                            name = name,
                            category = ServiceCategory.OTHER, // Simpified
                            description = description,
                            startingPrice = price,
                            pricingType = PricingType.STARTING_AT,
                            estimatedDuration = ServiceDuration.TWO_TO_THREE_HOURS,
                            tags = emptyList()
                        )
                    }
                },
                enabled = addState !is UiState.Loading && name.isNotBlank() && priceStr.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
