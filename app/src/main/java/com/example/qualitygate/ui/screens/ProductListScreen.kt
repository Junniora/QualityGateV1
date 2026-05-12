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

    val providers = listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis")

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
                    
                    // Buscador Arreglado (Full Width)
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
                                    FilterChip(selected = selectedStatus == s, onClick = { selectedStatus = if(selectedStatus == s) null else s }, label = { Text(s.name) })
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
    val templates = MilestoneTemplates.NEW_PRODUCT_MILESTONES
    fun getFutureDate(d: Int) = Timestamp(Date(System.currentTimeMillis() + d * 86400000L))
    fun getPastDate(d: Int) = Timestamp(Date(System.currentTimeMillis() - d * 86400000L))

    val toyota = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "TOY-ALT-045", serialNumber = "TY-874", description = "Alternador 12V", provider = "Toyota", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.PLANNING)
    viewModel.registerFullProduct(toyota, templates.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getFutureDate(i), plannedEnd = getFutureDate(i+2)) }) {}

    val vw = Product(classification = ProductClassification.NUEVO_PRODUCTO, partNumber = "VW-GOLF-DASH", serialNumber = "VW-222", description = "Cluster Digital", provider = "Toyota", supervisorName = supervisorName, supervisorId = supervisorId, status = ProductStatus.COMPLETED)
    viewModel.registerFullProduct(vw, templates.mapIndexed { i, n -> Milestone(name = n, order = i, plannedStart = getPastDate(10), plannedEnd = getPastDate(5), realStart = getPastDate(9), realEnd = getPastDate(4), status = MilestoneStatus.COMPLETADO) }) {}
}

@Composable
fun AppleProductItem(product: Product, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.partNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${product.provider} | ${product.classification.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Responsable: ${product.supervisorName}", style = MaterialTheme.typography.labelSmall)
            }
            Surface(color = if(product.status == ProductStatus.COMPLETED) Color(0xFF34C759).copy(0.1f) else Color.Gray.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                Text(product.status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
