package com.example.qualitygate.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val feedbacks by productViewModel.productFeedback.collectAsState()
    val currentFeedback = feedbacks[productId] ?: emptyList()
    
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
        productViewModel.fetchFeedback(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P/N: ${product?.partNumber ?: ""}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (product == null || isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { ProductInfoSection(product) }

                item {
                    Text("Fotos del Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.photos) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Actividades (Milestones)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                "${milestones.count { it.status == MilestoneStatus.COMPLETADO }}/${milestones.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                items(milestones) { milestone ->
                    MilestoneCard(milestone, userRole, product.status) { updated ->
                        productViewModel.updateMilestone(updated) { if (it) refreshMilestones() }
                    }
                }

                item {
                    StatusActionSection(product, userRole, milestones) { newStatus, comment ->
                        if (comment != null) {
                            productViewModel.addFeedback(productId, "system", comment)
                        }
                        productViewModel.updateProductStatus(productId, newStatus) { if (it) onBack() }
                    }
                }

                if (currentFeedback.isNotEmpty()) {
                    item {
                        Text("Historial de Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(currentFeedback) { fb ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(fb.comment, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    formatDate(fb.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun ProductInfoSection(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(product.status) {
                        ProductStatus.PLANNING -> Color.Gray
                        ProductStatus.ON_GOING -> Color(0xFF2196F3)
                        ProductStatus.COMPLETED -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(product.status.name.replace("_", " "), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Clasificación: ${product.classification.name.replace("_", " ")}")
            Text("Responsable: ${product.supervisorName}")
            Text("Registrado: ${formatDate(product.registrationDate)}")
        }
    }
}

@Composable
fun MilestoneCard(milestone: Milestone, userRole: UserRole, productStatus: ProductStatus, onUpdate: (Milestone) -> Unit) {
    val context = LocalContext.current
    
    fun showDatePicker(onDateSelected: (Timestamp) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            val sel = Calendar.getInstance()
            sel.set(y, m, d)
            onDateSelected(Timestamp(sel.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.status == MilestoneStatus.COMPLETADO) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${milestone.order + 1}. ${milestone.name}", fontWeight = FontWeight.SemiBold)
            
            // Fechas Planeadas
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateButton(
                    label = "Plan Inicio",
                    date = milestone.plannedStart,
                    enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.PLANNING,
                    onClick = { showDatePicker { onUpdate(milestone.copy(plannedStart = it)) } }
                )
                DateButton(
                    label = "Plan Fin",
                    date = milestone.plannedEnd,
                    enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.PLANNING,
                    onClick = { showDatePicker { onUpdate(milestone.copy(plannedEnd = it)) } }
                )
            }

            // Fechas Reales (Solo en ON_GOING)
            if (productStatus == ProductStatus.ON_GOING) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateButton(
                        label = "Real Inicio",
                        date = milestone.realStart,
                        enabled = userRole == UserRole.SUPERVISOR,
                        onClick = { showDatePicker { onUpdate(milestone.copy(realStart = it)) } }
                    )
                    DateButton(
                        label = "Real Fin",
                        date = milestone.realEnd,
                        enabled = userRole == UserRole.SUPERVISOR,
                        onClick = { 
                            showDatePicker { 
                                val newMilestone = milestone.copy(realEnd = it, status = MilestoneStatus.COMPLETADO)
                                onUpdate(newMilestone)
                            } 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateButton(label: String, date: Timestamp?, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            if (date != null) formatDate(date) else label,
            fontSize = 11.sp
        )
    }
}

@Composable
fun StatusActionSection(
    product: Product, 
    userRole: UserRole, 
    milestones: List<Milestone>,
    onAction: (ProductStatus, String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    var nextStatusState by remember { mutableStateOf(ProductStatus.PLANNING) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Agregar Comentario de Feedback") },
            text = {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Motivo del rechazo") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { onAction(nextStatusState, comment); showDialog = false }) {
                    Text("Confirmar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        when {
            // SUPERVISOR: De Planeación a Pre-Revisión
            userRole == UserRole.SUPERVISOR && product.status == ProductStatus.PLANNING -> {
                val allPlanned = milestones.all { it.plannedStart != null && it.plannedEnd != null }
                Button(
                    onClick = { onAction(ProductStatus.PRE_REVISION, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = allPlanned
                ) {
                    Text("Enviar a Pre-Revisión")
                }
            }

            // REVISOR: Pre-Revisión (Aprobar/Rechazar planeación)
            userRole == UserRole.REVISOR && product.status == ProductStatus.PRE_REVISION -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.ON_GOING, "Planeación aprobada") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(4.dp)); Text("Aprobar")
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.PLANNING; showDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, null); Spacer(Modifier.width(4.dp)); Text("Rechazar")
                    }
                }
            }

            // SUPERVISOR: On Going a Revisión Final
            userRole == UserRole.SUPERVISOR && product.status == ProductStatus.ON_GOING -> {
                val allCompleted = milestones.all { it.status == MilestoneStatus.COMPLETADO }
                Button(
                    onClick = { onAction(ProductStatus.FINAL_REVISION, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = allCompleted
                ) {
                    Text("Enviar a Revisión Final")
                }
            }

            // REVISOR: Revisión Final
            userRole == UserRole.REVISOR && product.status == ProductStatus.FINAL_REVISION -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.APROBACION_FINAL, "Revisión técnica exitosa") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aprobar Técnica")
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.ON_GOING; showDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Rechazar")
                    }
                }
            }

            // APROBADOR: Aprobación Final
            userRole == UserRole.APROBADOR && product.status == ProductStatus.APROBACION_FINAL -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.COMPLETED, "Producto Aprobado") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("APROBACIÓN FINAL")
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.FINAL_REVISION; showDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Rechazar")
                    }
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
