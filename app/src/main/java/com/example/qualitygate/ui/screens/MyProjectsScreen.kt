package com.example.qualitygate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.qualitygate.data.model.Product
import com.example.qualitygate.data.model.ProductStatus
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProjectsScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.productList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val myProducts = products.filter { it.supervisorId == currentUser?.id }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Mis Proyectos", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (myProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay proyectos registrados", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myProducts) { product ->
                    MyProjectItem(
                        product = product,
                        onEdit = { productToEdit = it },
                        onDelete = { productToDelete = it },
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }

    if (productToEdit != null) {
        EditProductDialog(
            product = productToEdit!!,
            onDismiss = { productToEdit = null },
            onConfirm = { updatedProduct ->
                productViewModel.updateProduct(updatedProduct) { success ->
                    if (success) productToEdit = null
                }
            }
        )
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar Proyecto", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de eliminar '${productToDelete!!.partNumber}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productViewModel.deleteProduct(productToDelete!!.id) { success ->
                            if (success) productToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun MyProjectItem(product: Product, onEdit: (Product) -> Unit, onDelete: (Product) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.partNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(product.provider, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(product.description, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = { onEdit(product) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { onDelete(product) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var partNumber by remember { mutableStateOf(product.partNumber) }
    var serialNumber by remember { mutableStateOf(product.serialNumber) }
    var description by remember { mutableStateOf(product.description) }
    var provider by remember { mutableStateOf(product.provider) }
    var expanded by remember { mutableStateOf(false) }
    val providers = listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Detalles", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = partNumber, onValueChange = { partNumber = it }, label = { Text("Número de Pieza") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Número de Serie") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, shape = RoundedCornerShape(12.dp))
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = provider, onValueChange = {}, readOnly = true, label = { Text("Proveedor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        providers.forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { provider = p; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(product.copy(partNumber = partNumber, serialNumber = serialNumber, description = description, provider = provider))
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp)
    )
}
