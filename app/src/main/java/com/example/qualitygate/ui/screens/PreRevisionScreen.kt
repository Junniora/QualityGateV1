package com.example.qualitygate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualitygate.data.model.Product
import com.example.qualitygate.data.model.ProductStatus
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreRevisionScreen(
    productViewModel: ProductViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val preRevisionProducts = products.filter { it.status == ProductStatus.PLANNING }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Pre-Revisión", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (preRevisionProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay productos pendientes de revisión", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(preRevisionProducts) { product ->
                    PreRevisionItem(
                        product = product,
                        onApprove = { 
                            productViewModel.updateProductStatus(product.id, ProductStatus.ON_GOING) { }
                        },
                        onReject = {
                            productViewModel.updateProductStatus(product.id, ProductStatus.RECHAZADO) { }
                        },
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun PreRevisionItem(
    product: Product,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.partNumber,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = product.classification.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                TextButton(onClick = onClick) {
                    Text("Ver Fechas", fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Supervisor: ${product.supervisorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aprobar", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechazar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
