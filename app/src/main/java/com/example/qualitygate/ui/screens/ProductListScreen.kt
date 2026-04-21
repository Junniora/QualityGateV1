package com.example.qualitygate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
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
import com.example.qualitygate.data.model.UserRole
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel

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
    var isGeneratingDemo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Explorar Productos", 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        if (currentUser?.role == UserRole.SUPERVISOR) {
                            IconButton(onClick = {
                                isGeneratingDemo = true
                                val demoData = listOf(
                                    Triple("TOY-ALT-2GR-045", "TY20260420-000874", "Alternador para motor 2GR, 12V" to "Toyota"),
                                    Triple("SUB-INJ-BOX-778", "SB20260420-5562", "Inyector para motor bóxer 2.0L" to "Subaru"),
                                    Triple("FRD-BRK-ABS-321", "FD20260420-7781", "Módulo ABS sistema de frenos" to "Ford"),
                                    Triple("MAZ-SNS-SKY-112", "MZ9834521", "Sensor de temperatura motor Skyactiv" to "Mazda"),
                                    Triple("STL-TRN-AUTO-991", "STL00456789", "Componente de transmisión automática" to "Stellantis")
                                )
                                demoData.forEach { (pn, sn, extra) ->
                                    productViewModel.registerProduct(
                                        classification = ProductClassification.NUEVO_PRODUCTO,
                                        partNumber = pn,
                                        serialNumber = sn,
                                        description = extra.first,
                                        provider = extra.second,
                                        supervisorName = currentUser?.name ?: "Demo User",
                                        supervisorId = currentUser?.id ?: "demo_id",
                                        photoUris = emptyList()
                                    )
                                }
                                isGeneratingDemo = false
                            }) {
                                if (isGeneratingDemo) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                else Icon(Icons.Default.AutoAwesome, contentDescription = "Demo Data", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por P/N o Responsable") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            if (currentUser?.role == UserRole.SUPERVISOR) {
                FloatingActionButton(
                    onClick = onAddProductClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                }
            }
        }
    ) { padding ->
        val filteredProducts = products.filter { 
            it.partNumber.contains(searchQuery, ignoreCase = true) || 
            it.supervisorName.contains(searchQuery, ignoreCase = true) ||
            it.provider.contains(searchQuery, ignoreCase = true)
        }

        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay resultados", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    AppleProductItem(product = product, onClick = { onProductClick(product) })
                }
            }
        }
    }
}

@Composable
fun AppleProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.partNumber, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
                Text(
                    text = "${product.provider} | ${product.classification.name.replace("_", " ")}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1
                )
            }
            
            Surface(
                color = when(product.status) {
                    ProductStatus.PLANNING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ProductStatus.ON_GOING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ProductStatus.COMPLETED -> Color(0xFF34C759).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                },
                contentColor = when(product.status) {
                    ProductStatus.PLANNING -> MaterialTheme.colorScheme.secondary
                    ProductStatus.ON_GOING -> MaterialTheme.colorScheme.tertiary
                    ProductStatus.COMPLETED -> Color(0xFF34C759)
                    else -> MaterialTheme.colorScheme.error
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = product.status.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
