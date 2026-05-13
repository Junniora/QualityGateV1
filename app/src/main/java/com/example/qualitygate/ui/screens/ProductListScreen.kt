package com.example.qualitygate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualitygate.data.model.*
import com.example.qualitygate.data.util.MilestoneTemplates
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel
import com.google.firebase.Timestamp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onProductClick: (Product) -> Unit,
    onAddProductClick: () -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var selectedClass by remember { mutableStateOf<ProductClassification?>(null) }
    var selectedStatus by remember { mutableStateOf<ProductStatus?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var isGeneratingDemo by remember { mutableStateOf(false) }

    val providers = listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis", "Volkswagen")

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.partNumber.contains(searchQuery, true) || 
                          product.supervisorName.contains(searchQuery, true) ||
                          product.serialNumber.contains(searchQuery, true)
        val matchesProvider = selectedProvider == null || product.provider == selectedProvider
        val matchesClass = selectedClass == null || product.classification == selectedClass
        val matchesStatus = selectedStatus == null || product.status == selectedStatus
        
        matchesSearch && matchesProvider && matchesClass && matchesStatus
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Explorar Productos", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { showFilters = !showFilters }) {
                                Icon(Icons.Default.FilterList, null, tint = if(showFilters) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                            if (currentUser?.role == UserRole.SUPERVISOR) {
                                IconButton(onClick = {
                                    isGeneratingDemo = true
                                    generateDemoScenarios(productViewModel, currentUser)
                                    isGeneratingDemo = false
                                }) {
                                    if (isGeneratingDemo) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    else Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar P/N, S/N o Responsable") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )
                    }

                    AnimatedVisibility(visible = showFilters) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Marca:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(providers) { p ->
                                    FilterChip(selected = selectedProvider == p, onClick = { selectedProvider = if(selectedProvider == p) null else p }, label = { Text(p) })
                                }
                            }
                            Text("Estado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(ProductStatus.entries.toList()) { s ->
                                    FilterChip(selected = selectedStatus == s, onClick = { selectedStatus = if(selectedStatus == s) null else s }, label = { Text(s.name.replace("_", " ")) })
                                }
                            }
                            TextButton(onClick = { selectedProvider = null; selectedStatus = null; searchQuery = "" }, modifier = Modifier.align(Alignment.End)) {
                                Text("Limpiar Filtros", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentUser?.role == UserRole.SUPERVISOR) {
                FloatingActionButton(onClick = onAddProductClick) { Icon(Icons.Default.Add, null) }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredProducts) { product ->
                AppleProductItem(product = product, onClick = { onProductClick(product) })
            }
        }
    }
}

fun generateDemoScenarios(viewModel: ProductViewModel, currentUser: User?) {
    val supervisorName = currentUser?.name ?: "Demo User"
    val supervisorId = currentUser?.id ?: "demo_id"
    val templatesNew = MilestoneTemplates.NEW_PRODUCT_MILESTONES
    val templatesTrans = MilestoneTemplates.TRANSFER_PRODUCT_MILESTONES

    fun getFutureDate(d: Int) = Timestamp(Date(System.currentTimeMillis() + d * 86400000L))
    fun getPastDate(d: Int) = Timestamp(Date(System.currentTimeMillis() - d * 86400000L))

    // 1. Toyota - Planning
    val toyota = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "TOY-ALT-045", serialNumber = "TY-874", description = "Alternador 12V", provider = "Toyota", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.PLANNING)
    viewModel.registerFullProduct(toyota, templatesNew.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getFutureDate(i), plannedEnd = getFutureDate(i+2)) }) { pid ->
        if(pid != null) viewModel.addFeedback(pid, supervisorId, supervisorName, "SUPERVISOR", "Producto registrado en sistema. Pendiente de planeación de fechas.")
    }

    // 2. Volkswagen - Completed
    val vw = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "VW-GOLF-DASH", serialNumber = "VW-222", description = "Cluster Digital", provider = "Volkswagen", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.COMPLETED)
    viewModel.registerFullProduct(vw, templatesNew.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getPastDate(15), plannedEnd = getPastDate(10), realStart = getPastDate(14), realEnd = getPastDate(9), status = MilestoneStatus.COMPLETADO) }) { pid ->
        if(pid != null) {
            viewModel.addFeedback(pid, supervisorId, supervisorName, "SUPERVISOR", "Registro inicial y planeación finalizada.")
            viewModel.addFeedback(pid, "revisor_id", "Ing. Calidad", "REVISOR", "Revisión técnica de hitos aprobada satisfactoriamente.")
            viewModel.addFeedback(pid, "aprobador_id", "Gerencia Planta", "APROBADOR", "Proyecto liberado oficialmente para producción masiva.")
        }
    }

    // 3. Ford - On Going
    val ford = Product(classification = ProductClassification.TRANSFERENCIA_PRODUCTO, partNumber = "FRD-F150-CAM", serialNumber = "FD-990", description = "Cámara Reversa", provider = "Ford", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.ON_GOING)
    viewModel.registerFullProduct(ford, templatesTrans.mapIndexed { i, n -> 
        val status = if (i < 5) MilestoneStatus.COMPLETADO else if (i == 5) MilestoneStatus.ON_GOING else MilestoneStatus.PENDIENTE
        val realStart = if (i <= 5) getPastDate(5-i) else null
        val realEnd = if (i < 5) getPastDate(4-i) else null
        Milestone(name = n, order = i, plannedStart = getPastDate(10-i), plannedEnd = getPastDate(8-i), realStart = realStart, realEnd = realEnd, status = status) 
    }) { pid ->
        if(pid != null) {
            viewModel.addFeedback(pid, supervisorId, supervisorName, "SUPERVISOR", "Inicio de transferencia de línea de producción.")
            viewModel.addFeedback(pid, "revisor_id", "Ing. Calidad", "REVISOR", "Cronograma de transferencia validado. Fase de ejecución iniciada.")
        }
    }

    // 4. Subaru - Pre Revision
    val subaru = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "SUB-BRZ-STR", serialNumber = "SB-112", description = "Volante Deportivo", provider = "Subaru", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.PRE_REVISION)
    viewModel.registerFullProduct(subaru, templatesNew.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getFutureDate(i+1), plannedEnd = getFutureDate(i+3)) }) { pid ->
        if(pid != null) viewModel.addFeedback(pid, supervisorId, supervisorName, "SUPERVISOR", "Plan de hitos enviado para revisión inicial de fechas.")
    }

    // 5. Mazda - Final Revision
    val mazda = Product(classification = ProductClassification.TRANSFERENCIA_PRODUCTO, partNumber = "MAZ-CX5-LED", serialNumber = "MZ-445", description = "Faro LED", provider = "Mazda", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.FINAL_REVISION)
    viewModel.registerFullProduct(mazda, templatesTrans.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getPastDate(20-i), plannedEnd = getPastDate(18-i), realStart = getPastDate(19-i), realEnd = getPastDate(17-i), status = MilestoneStatus.COMPLETADO) }) { pid ->
        if(pid != null) viewModel.addFeedback(pid, supervisorId, supervisorName, "SUPERVISOR", "Todas las actividades de transferencia completadas. Solicitud de cierre técnico enviada.")
    }

    // 6. Stellantis - Aprobación Final
    val stellantis = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "STL-RAM-SUSP", serialNumber = "ST-778", description = "Módulo Suspensión", provider = "Stellantis", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.APROBACION_FINAL)
    viewModel.registerFullProduct(stellantis, templatesNew.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getPastDate(30-i), plannedEnd = getPastDate(28-i), realStart = getPastDate(29-i), realEnd = getPastDate(27-i), status = MilestoneStatus.COMPLETADO) }) { pid ->
        if(pid != null) {
            viewModel.addFeedback(pid, "revisor_id", "Ing. Calidad", "REVISOR", "Validación técnica final completada. Proyecto listo para aprobación de gerencia.")
        }
    }
}

@Composable
fun AppleProductItem(product: Product, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.partNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${product.provider} | ${product.classification.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Responsable: ${product.supervisorName}", style = MaterialTheme.typography.labelSmall)
            }
            Surface(
                color = when(product.status) {
                    ProductStatus.COMPLETED -> Color(0xFF34C759).copy(0.1f)
                    ProductStatus.ON_GOING -> Color(0xFF007AFF).copy(0.1f)
                    ProductStatus.RECHAZADO -> Color(0xFFFF3B30).copy(0.1f)
                    else -> Color.Gray.copy(0.1f)
                }, 
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    product.status.name.replace("_", " "), 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold,
                    color = when(product.status) {
                        ProductStatus.COMPLETED -> Color(0xFF34C759)
                        ProductStatus.ON_GOING -> Color(0xFF007AFF)
                        ProductStatus.RECHAZADO -> Color(0xFFFF3B30)
                        else -> Color.DarkGray
                    }
                )
            }
        }
    }
}
