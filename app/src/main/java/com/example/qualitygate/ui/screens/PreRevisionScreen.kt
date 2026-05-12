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
import com.example.qualitygate.data.model.*
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreRevisionScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<ProductStatus?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    // Estado para el diálogo de rechazo
    var productToReject by remember { mutableStateOf<Product?>(null) }
    var rejectComment by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery, selectedProvider, selectedStatus) {
        products.filter { product ->
            val matchesSearch = product.partNumber.contains(searchQuery, true) || 
                              product.supervisorName.contains(searchQuery, true)
            val matchesProvider = selectedProvider == null || product.provider == selectedProvider
            val matchesStatus = if (selectedStatus != null) {
                product.status == selectedStatus
            } else {
                product.status == ProductStatus.PRE_REVISION || product.status == ProductStatus.FINAL_REVISION
            }
            matchesSearch && matchesProvider && matchesStatus
        }
    }

    if (productToReject != null) {
        AlertDialog(
            onDismissRequest = { productToReject = null },
            title = { Text("Motivo del Rechazo", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectComment,
                    onValueChange = { rejectComment = it },
                    label = { Text("Escribe la observación técnica...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val backStatus = if (productToReject!!.status == ProductStatus.PRE_REVISION) 
                            ProductStatus.PLANNING else ProductStatus.ON_GOING
                        
                        productViewModel.addFeedback(
                            productId = productToReject!!.id,
                            userId = currentUser?.id ?: "",
                            userName = currentUser?.name ?: "Revisor",
                            userRole = "REVISOR",
                            comment = "RECHAZADO: $rejectComment"
                        )
                        productViewModel.updateProductStatus(productToReject!!.id, backStatus) {
                            productToReject = null
                            rejectComment = ""
                        }
                    },
                    enabled = rejectComment.isNotBlank()
                ) { Text("Confirmar Rechazo") }
            },
            dismissButton = { TextButton(onClick = { productToReject = null }) { Text("Cancelar") } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Revisión", fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp) },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, null, tint = if(showFilters) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar P/N o Responsable") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                AnimatedVisibility(visible = showFilters) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Filtrar por Marca:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis")) { p ->
                                FilterChip(selected = selectedProvider == p, onClick = { selectedProvider = if(selectedProvider == p) null else p }, label = { Text(p) })
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredProducts) { product ->
                PreRevisionItem(
                    product = product,
                    onApprove = { 
                        val nextStatus = if (product.status == ProductStatus.PRE_REVISION) 
                            ProductStatus.ON_GOING else ProductStatus.APROBACION_FINAL
                        
                        productViewModel.addFeedback(
                            productId = product.id,
                            userId = currentUser?.id ?: "",
                            userName = currentUser?.name ?: "Revisor",
                            userRole = "REVISOR",
                            comment = "APROBADO: Revisión completada con éxito."
                        )
                        productViewModel.updateProductStatus(product.id, nextStatus) { }
                    },
                    onReject = { productToReject = product },
                    onSeeActivities = { onProductClick(product) }
                )
            }
        }
    }
}

@Composable
fun PreRevisionItem(product: Product, onApprove: () -> Unit, onReject: () -> Unit, onSeeActivities: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { onSeeActivities() }) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.partNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${product.provider} | ${product.status.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                OutlinedButton(onClick = onSeeActivities, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ACTIVIDADES", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))) {
                    Text("APROBAR", fontWeight = FontWeight.Bold)
                }
                Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("RECHAZAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
