package com.example.qualitygate.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.qualitygate.data.model.*
import com.example.qualitygate.ui.viewmodel.ProductViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    productViewModel: ProductViewModel,
    userRole: UserRole,
    onBack: () -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val product = products.find { it.id == productId }
    
    var milestones by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshMilestones() {
        productViewModel.fetchMilestones(productId) { result ->
            milestones = result
            isLoading = false
        }
    }

    LaunchedEffect(productId) {
        refreshMilestones()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle: ${product?.partNumber ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null || isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                ProductInfoCard(product)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Milestones (${milestones.size})", style = MaterialTheme.typography.titleLarge)
                    if (milestones.isEmpty() && !isLoading) {
                        TextButton(onClick = { refreshMilestones() }) {
                            Text("Actualizar")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(milestones) { milestone ->
                        MilestoneItem(milestone, userRole) { updatedMilestone ->
                            productViewModel.updateMilestone(updatedMilestone) { success ->
                                if (success) refreshMilestones()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductInfoCard(product: Product) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Información General", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("P/N: ${product.partNumber}")
            Text("Clasificación: ${product.classification.name.replace("_", " ")}")
            Text("Responsable: ${product.supervisorName}")
            Text("Estado Actual: ${product.status.name}")
        }
    }
}

@Composable
fun MilestoneItem(milestone: Milestone, userRole: UserRole, onUpdate: (Milestone) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDatePicker(onDateSelected: (Timestamp) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                onDateSelected(Timestamp(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(milestone.status) {
                MilestoneStatus.COMPLETADO -> MaterialTheme.colorScheme.primaryContainer
                MilestoneStatus.RECHAZADO -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${milestone.order + 1}. ${milestone.name}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Estado: ${milestone.status}", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { if (userRole == UserRole.SUPERVISOR) showDatePicker { onUpdate(milestone.copy(plannedStart = it)) } },
                    modifier = Modifier.weight(1f),
                    enabled = userRole == UserRole.SUPERVISOR
                ) {
                    Text(text = if (milestone.plannedStart != null) "Inicia: ${formatDate(milestone.plannedStart)}" else "Planear Inicio", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { if (userRole == UserRole.SUPERVISOR) showDatePicker { onUpdate(milestone.copy(plannedEnd = it)) } },
                    modifier = Modifier.weight(1f),
                    enabled = userRole == UserRole.SUPERVISOR
                ) {
                    Text(text = if (milestone.plannedEnd != null) "Fin: ${formatDate(milestone.plannedEnd)}" else "Planear Fin", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "N/A"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
