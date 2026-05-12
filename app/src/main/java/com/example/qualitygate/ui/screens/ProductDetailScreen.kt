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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
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

enum class MilestoneTaskStatus(val label: String, val color: Color) {
    DONE("DONE", Color(0xFF34C759)),
    DELAY("DELAY", Color(0xFFFF3B30)),
    ON_PLAN("ON PLAN", Color(0xFF007AFF))
}

fun calculateMilestoneStatus(milestone: Milestone): MilestoneTaskStatus {
    val today = Timestamp.now()
    return when {
        milestone.realEnd != null -> MilestoneTaskStatus.DONE
        milestone.plannedEnd != null && today.seconds > milestone.plannedEnd.seconds -> MilestoneTaskStatus.DELAY
        else -> MilestoneTaskStatus.ON_PLAN
    }
}

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
                title = { Text("Control de Tiempos: ${product?.partNumber ?: ""}", fontWeight = FontWeight.Bold) },
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

                // --- CARDS PRINCIPALES DE KPI ---
                val showKPI = product.status != ProductStatus.PLANNING && product.status != ProductStatus.COMPLETED

                if (showKPI && milestones.isNotEmpty()) {
                    val doneCount = milestones.count { calculateMilestoneStatus(it) == MilestoneTaskStatus.DONE }
                    val delayCount = milestones.count { calculateMilestoneStatus(it) == MilestoneTaskStatus.DELAY }
                    val total = milestones.size
                    val progress = if (total > 0) doneCount.toFloat() / total.toFloat() else 0f

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Estado de Avance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("$doneCount Completadas", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("$delayCount Delay", style = MaterialTheme.typography.bodySmall, color = if (delayCount > 0) Color.Red else Color.Unspecified, fontWeight = FontWeight.Bold)
                                        Text("${total - doneCount} Pendientes", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (delayCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (delayCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (delayCount > 0) Color.Red else Color(0xFF2E7D32)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            if (delayCount > 0) "Actividades Críticas (Delay)" else "Al día - Sin retrasos",
                                            fontWeight = FontWeight.Bold,
                                            color = if (delayCount > 0) Color.Red else Color(0xFF2E7D32)
                                        )
                                    }
                                    
                                    if (delayCount > 0) {
                                        Spacer(Modifier.height(12.dp))
                                        milestones.filter { calculateMilestoneStatus(it) == MilestoneTaskStatus.DELAY }.forEach {
                                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                                                Spacer(Modifier.width(8.dp))
                                                Text(it.name, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- CRONOGRAMA DE ACTIVIDADES ---
                val showActivities = product.status != ProductStatus.COMPLETED

                if (showActivities) {
                    item {
                        Text("Cronograma de Actividades (Fechas)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(milestones) { milestone ->
                        MilestoneCard(milestone, userRole, product.status) { updated ->
                            productViewModel.updateMilestone(updated) { if (it) refreshMilestones() }
                        }
                    }
                }

                item {
                    Text("Evidencia Fotográfica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (product.photos.isEmpty()) {
                        Text("Sin fotos registradas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Historial de Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(currentFeedback) { fb ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(fb.comment, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(product.status) {
                        ProductStatus.PLANNING -> Color.Gray
                        ProductStatus.ON_GOING -> Color(0xFF2196F3)
                        ProductStatus.COMPLETED -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(product.status.name.replace("_", " "), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Proveedor: ${product.provider}", style = MaterialTheme.typography.bodyMedium)
            Text("Serie: ${product.serialNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Responsable: ${product.supervisorName}", style = MaterialTheme.typography.bodyMedium)
            Text("Registrado: ${formatDate(product.registrationDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun MilestoneCard(milestone: Milestone, userRole: UserRole, productStatus: ProductStatus, onUpdate: (Milestone) -> Unit) {
    val context = LocalContext.current
    val taskStatus = remember(milestone) { calculateMilestoneStatus(milestone) }
    
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${milestone.order + 1}. ${milestone.name}", 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )

                // BOTÓN DE ELIMINAR FECHAS (Basurita)
                if (userRole == UserRole.SUPERVISOR && (productStatus == ProductStatus.PLANNING || productStatus == ProductStatus.ON_GOING)) {
                    IconButton(
                        onClick = {
                            val resetMilestone = if (productStatus == ProductStatus.PLANNING) {
                                milestone.copy(plannedStart = null, plannedEnd = null)
                            } else {
                                milestone.copy(realStart = null, realEnd = null, status = MilestoneStatus.PENDIENTE)
                            }
                            onUpdate(resetMilestone)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Borrar fechas",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Surface(
                    color = taskStatus.color.copy(alpha = 0.15f),
                    contentColor = taskStatus.color,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = taskStatus.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            
            // Fechas Planeadas
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text("Planificación:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateButton(
                    label = "Inicio",
                    date = milestone.plannedStart,
                    enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.PLANNING,
                    onClick = { showDatePicker { onUpdate(milestone.copy(plannedStart = it)) } }
                )
                DateButton(
                    label = "Cierre",
                    date = milestone.plannedEnd,
                    enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.PLANNING,
                    onClick = { showDatePicker { onUpdate(milestone.copy(plannedEnd = it)) } }
                )
            }

            // Fechas Reales
            val showRealDates = productStatus != ProductStatus.PLANNING && productStatus != ProductStatus.PRE_REVISION
            if (showRealDates) {
                Spacer(Modifier.height(12.dp))
                Text("Ejecución Real:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateButton(
                        label = "Empezar",
                        date = milestone.realStart,
                        enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.ON_GOING,
                        onClick = { showDatePicker { onUpdate(milestone.copy(realStart = it)) } }
                    )
                    DateButton(
                        label = "Terminar",
                        date = milestone.realEnd,
                        enabled = userRole == UserRole.SUPERVISOR && productStatus == ProductStatus.ON_GOING,
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
        modifier = Modifier.height(34.dp).widthIn(min = 100.dp),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            if (date != null) formatDate(date) else label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
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
            title = { Text("Feedback de Calidad", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Escribe el motivo del rechazo...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = { onAction(nextStatusState, comment); showDialog = false }) {
                    Text("Confirmar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        when {
            // SUPERVISOR: De Planeación a Pre-Revisión
            userRole == UserRole.SUPERVISOR && product.status == ProductStatus.PLANNING -> {
                val allPlanned = milestones.all { it.plannedStart != null && it.plannedEnd != null }
                Button(
                    onClick = { onAction(ProductStatus.PRE_REVISION, null) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = allPlanned && milestones.isNotEmpty()
                ) {
                    Text("Enviar Planeación a Ingeniería", fontWeight = FontWeight.Bold)
                }
            }

            // REVISOR: Pre-Revisión (Aprobar/Rechazar planeación)
            userRole == UserRole.REVISOR && product.status == ProductStatus.PRE_REVISION -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.ON_GOING, "Cronograma validado por ingeniería") },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                    ) {
                        Icon(Icons.Default.Check, null); Spacer(Modifier.width(4.dp)); Text("Validar Plan")
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.PLANNING; showDialog = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
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
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = allCompleted
                ) {
                    Text("Solicitar Cierre Técnico", fontWeight = FontWeight.Bold)
                }
            }

            // REVISOR: Revisión Final Técnica
            userRole == UserRole.REVISOR && product.status == ProductStatus.FINAL_REVISION -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.APROBACION_FINAL, "Revisión técnica final completada exitosamente") },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Aprobación Técnica", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.ON_GOING; showDialog = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Rechazar Cierre")
                    }
                }
            }

            // APROBADOR: Firma Final
            userRole == UserRole.APROBADOR && product.status == ProductStatus.APROBACION_FINAL -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAction(ProductStatus.COMPLETED, "Proyecto liberado para producción masiva") },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                    ) {
                        Text("LIBERACIÓN FINAL", fontWeight = FontWeight.ExtraBold)
                    }
                    Button(
                        onClick = { nextStatusState = ProductStatus.FINAL_REVISION; showDialog = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Observación")
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
