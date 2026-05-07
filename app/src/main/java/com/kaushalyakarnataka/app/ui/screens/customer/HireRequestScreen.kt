package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.DatePickerRow
import com.kaushalyakarnataka.app.ui.components.HireSuccessModal
import com.kaushalyakarnataka.app.ui.components.PrimaryButton
import com.kaushalyakarnataka.app.ui.components.PrimaryInputField
import com.kaushalyakarnataka.app.ui.components.TimeSlotPicker
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.HireViewModel
import java.time.LocalDate

@Composable
fun HireRequestScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HireViewModel = hiltViewModel()
) {
    val hireState by viewModel.bookingState.collectAsState()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTimeSlot by remember { mutableStateOf<String?>(null) }
    var serviceDescription by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    var showSuccessModal by remember { mutableStateOf(false) }

    val timeSlots = listOf("09:00 AM", "11:00 AM", "02:00 PM", "04:00 PM", "06:00 PM")

    LaunchedEffect(hireState) {
        if (hireState is UiState.Success) {
            showSuccessModal = true
        }
    }

    if (showSuccessModal) {
        HireSuccessModal(
            onDismiss = {
                showSuccessModal = false
                viewModel.clearBookingState()
                onBookingSuccess()
            },
            workerName = "the worker" // Simplification
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Request Service",
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
            Text(
                text = "When do you need the service?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            DatePickerRow(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Select Time Slot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            TimeSlotPicker(
                slots = timeSlots,
                selectedSlot = selectedTimeSlot,
                onSlotSelected = { selectedTimeSlot = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryInputField(
                value = serviceDescription,
                onValueChange = { serviceDescription = it },
                label = "Describe what you need done",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryInputField(
                value = address,
                onValueChange = { address = it },
                label = "Your Address"
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (hireState is UiState.Error) {
                Text(
                    text = (hireState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            PrimaryButton(
                text = "Send Request",
                onClick = {
                    if (selectedTimeSlot != null && serviceDescription.isNotBlank() && address.isNotBlank()) {
                        viewModel.submitBookingRequest(
                            serviceName = "Requested Service",
                            timeSlot = selectedTimeSlot!!,
                            address = address,
                            notes = serviceDescription
                        )
                    }
                },
                enabled = hireState !is UiState.Loading && selectedTimeSlot != null && serviceDescription.isNotBlank() && address.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
