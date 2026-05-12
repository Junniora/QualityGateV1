package com.example.qualitygate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import com.example.qualitygate.data.model.Product
import com.example.qualitygate.data.model.ProductClassification
import com.example.qualitygate.data.model.ProductStatus
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    
    // Estados de filtrado
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var selectedClass by remember { mutableStateOf<ProductClassification?>(null) }
    var selectedStatus by remember { mutableStateOf<ProductStatus?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    val providers = listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis")

    // Lógica de filtrado
    val filteredProducts = remember(products, searchQuery, selectedProvider, selectedClass, selectedStatus) {
        products.filter { product ->
            val matchesSearch = product.partNumber.contains(searchQuery, true) || 
                              product.supervisorName.contains(searchQuery, true)
            val matchesProvider = selectedProvider == null || product.provider == selectedProvider
            val matchesClass = selectedClass == null || product.classification == selectedClass
            val matchesStatus = if (selectedStatus != null) {
                product.status == selectedStatus
            } else {
                // Por defecto el aprobador ve proyectos en APROBACION_FINAL
                product.status == ProductStatus.APROBACION_FINAL
            }
            
            matchesSearch && matchesProvider && matchesClass && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { Text("Validación Final", fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp) },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                imageVector = Icons.Default.FilterList, 
                                contentDescription = "Filtros",
                                tint = if(showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                
                // Barra de Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar P/N o Responsable") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Panel de Filtros Industriales
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Marca / Proveedor:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(providers) { provider ->
                                FilterChip(
                                    selected = selectedProvider == provider,
                                    onClick = { selectedProvider = if(selectedProvider == provider) null else provider },
                                    label = { Text(provider) }
                                )
                            }
                        }
                        
                        Text("Clasificación:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProductClassification.entries.forEach { cls ->
                                FilterChip(
                                    selected = selectedClass == cls,
                                    onClick = { selectedClass = if(selectedClass == cls) null else cls },
                                    label = { Text(cls.name.replace("_", " ")) }
                                )
                            }
                        }

                        Text("Estado del Proceso:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ProductStatus.entries.toList()) { status ->
                                FilterChip(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = if(selectedStatus == status) null else status },
                                    label = { Text(status.name) }
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = { 
                                selectedProvider = null
                                selectedClass = null
                                selectedStatus = null
                                searchQuery = ""
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Restablecer Filtros", color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp), 
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sin resultados para los filtros", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    ApprovalItem(
                        product = product,
                        onApprove = { 
                            productViewModel.updateProductStatus(product.id, ProductStatus.COMPLETED) { }
                        },
                        onReject = {
                            productViewModel.updateProductStatus(product.id, ProductStatus.RECHAZADO) { }
                        },
                        onSeeActivities = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalItem(
    product: Product,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSeeActivities: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onSeeActivities() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.partNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${product.provider} | ${product.classification.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = product.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
                
                // BOTÓN VER ACTIVIDADES
                OutlinedButton(
                    onClick = onSeeActivities,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("VER ACTIVIDADES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Responsable: ${product.supervisorName}", style = MaterialTheme.typography.bodySmall)
            Text("S/N: ${product.serialNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VALIDAR", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RECHAZAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
