package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.PricingType
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.ServiceDuration
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.PrimaryButton
import com.kaushalyakarnataka.app.ui.theme.Primary
import com.kaushalyakarnataka.app.ui.theme.PrimaryTint
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.ServiceViewModel
import kotlinx.coroutines.launch

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
    var selectedCategory by remember { mutableStateOf(ServiceCategory.ELECTRICIAN) }
    var selectedDuration by remember { mutableStateOf(ServiceDuration.ONE_HOUR) }
    var selectedPricing by remember { mutableStateOf(PricingType.STARTING_AT) }
    
    val addState by viewModel.addServiceState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(addState) {
        when (val s = addState) {
            is UiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Service added successfully!") }
                viewModel.clearState()
                onSuccess()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(s.message) }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Add Service", onBackClick = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Service Name") },
                placeholder = { Text("e.g. Home Wiring, Fan Installation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Picker
            Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            val categories = listOf(
                ServiceCategory.ELECTRICIAN to Icons.Default.ElectricalServices,
                ServiceCategory.PLUMBER to Icons.Default.Plumbing,
                ServiceCategory.CARPENTER to Icons.Default.Carpenter,
                ServiceCategory.PAINTER to Icons.Default.FormatPaint,
                ServiceCategory.CLEANER to Icons.Default.CleaningServices,
                ServiceCategory.AC_TECH to Icons.Default.AcUnit,
                ServiceCategory.OTHER to Icons.Default.Build,
            )
            val rows = categories.chunked(4)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (cat, icon) ->
                        val isSelected = selectedCategory == cat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryTint else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(8.dp)
                        ) {
                            Icon(icon, null, tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                            Text(cat.displayName, style = MaterialTheme.typography.labelSmall, color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Describe what's included in this service...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                label = { Text("Starting Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("₹ ", color = Primary, fontWeight = FontWeight.Bold) }
            )

            // Duration
            Text("Estimated Duration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiceDuration.values().forEach { duration ->
                    val isSelected = selectedDuration == duration
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedDuration = duration }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            duration.displayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Pricing type
            Text("Pricing Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PricingType.values().forEach { type ->
                    val isSelected = selectedPricing == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedPricing = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            type.displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (addState is UiState.Loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                PrimaryButton(
                    text = "Save Service",
                    onClick = {
                        val price = priceStr.toIntOrNull() ?: 0
                        if (name.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a service name") }
                            return@PrimaryButton
                        }
                        if (price <= 0) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid price") }
                            return@PrimaryButton
                        }
                        viewModel.addService(
                            name = name.trim(),
                            category = selectedCategory,
                            description = description.trim(),
                            startingPrice = price,
                            pricingType = selectedPricing,
                            estimatedDuration = selectedDuration,
                            tags = emptyList()
                        )
                    },
                    enabled = addState !is UiState.Loading && name.isNotBlank() && priceStr.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
