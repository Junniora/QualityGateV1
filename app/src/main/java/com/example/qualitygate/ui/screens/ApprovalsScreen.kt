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
fun ApprovalsScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    // Estado para rechazo
    var productToReject by remember { mutableStateOf<Product?>(null) }
    var rejectComment by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery, selectedProvider) {
        products.filter { product ->
            val matchesSearch = product.partNumber.contains(searchQuery, true) || 
                              product.supervisorName.contains(searchQuery, true)
            val matchesProvider = selectedProvider == null || product.provider == selectedProvider
            val matchesStatus = product.status == ProductStatus.APROBACION_FINAL
            
            matchesSearch && matchesProvider && matchesStatus
        }
    }

    if (productToReject != null) {
        AlertDialog(
            onDismissRequest = { productToReject = null },
            title = { Text("Motivo de Observación", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectComment,
                    onValueChange = { rejectComment = it },
                    label = { Text("Escribe el motivo del rechazo...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productViewModel.addFeedback(
                            productId = productToReject!!.id,
                            userId = currentUser?.id ?: "",
                            userName = currentUser?.name ?: "Gerente",
                            userRole = "APROBADOR",
                            comment = "RECHAZO FINAL: $rejectComment"
                        )
                        productViewModel.updateProductStatus(productToReject!!.id, ProductStatus.FINAL_REVISION) {
                            productToReject = null
                            rejectComment = ""
                        }
                    },
                    enabled = rejectComment.isNotBlank()
                ) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { productToReject = null }) { Text("Cerrar") } }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { Text("Validación Final", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, null, tint = if(showFilters) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por P/N...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }
    ) { padding ->
        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sin validaciones pendientes", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    ApprovalItem(
                        product = product,
                        onApprove = { 
                            productViewModel.addFeedback(
                                productId = product.id,
                                userId = currentUser?.id ?: "",
                                userName = currentUser?.name ?: "Gerente",
                                userRole = "APROBADOR",
                                comment = "APROBACIÓN FINAL: Proyecto liberado para producción."
                            )
                            productViewModel.updateProductStatus(product.id, ProductStatus.COMPLETED) { }
                        },
                        onReject = { productToReject = product },
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.partNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${product.provider} | S/N: ${product.serialNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                OutlinedButton(onClick = onSeeActivities, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, null, modifier = Modifier.size(16.dp))
                    Text(" AUDITAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Text("VALIDAR", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("RECHAZAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
