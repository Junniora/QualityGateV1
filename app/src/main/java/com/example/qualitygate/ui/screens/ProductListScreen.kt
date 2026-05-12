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
                                generateDemoScenarios(productViewModel, currentUser)
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
                    placeholder = { Text("Buscar P/N, Marca o Responsable") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
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
                Text("Sin productos registrados", color = MaterialTheme.colorScheme.secondary)
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

fun generateDemoScenarios(viewModel: ProductViewModel, currentUser: User?) {
    val supervisorName = currentUser?.name ?: "Demo User"
    val supervisorId = currentUser?.id ?: "demo_id"
    val templates = MilestoneTemplates.NEW_PRODUCT_MILESTONES

    fun getFutureDate(days: Int): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return Timestamp(cal.time)
    }

    fun getPastDate(days: Int): Timestamp {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return Timestamp(cal.time)
    }

    // 1. Toyota - PLANNING
    val toyota = Product(
        classification = ProductClassification.NUEVO_PRODUCTO,
        partNumber = "TOY-ALT-2GR-045",
        serialNumber = "TY2026-874",
        description = "Alternador 12V",
        provider = "Toyota",
        supervisorName = supervisorName,
        supervisorId = supervisorId,
        status = ProductStatus.PLANNING
    )
    val toyotaHitos = templates.mapIndexed { i, name ->
        Milestone(name = name, order = i, plannedStart = getFutureDate(i), plannedEnd = getFutureDate(i + 2), status = MilestoneStatus.PENDIENTE)
    }
    viewModel.registerFullProduct(toyota, toyotaHitos) {}

    // 2. Ford - PRE_REVISION
    val ford = Product(
        classification = ProductClassification.NUEVO_PRODUCTO,
        partNumber = "FRD-BRK-ABS-321",
        serialNumber = "FD2026-7781",
        description = "Módulo ABS",
        provider = "Ford",
        supervisorName = supervisorName,
        supervisorId = supervisorId,
        status = ProductStatus.PRE_REVISION
    )
    val fordHitos = templates.mapIndexed { i, name ->
        Milestone(name = name, order = i, plannedStart = getFutureDate(i), plannedEnd = getFutureDate(i + 1), status = MilestoneStatus.PENDIENTE)
    }
    viewModel.registerFullProduct(ford, fordHitos) {}

    // 3. Mazda - ON_GOING (DELAY)
    val mazda = Product(
        classification = ProductClassification.NUEVO_PRODUCTO,
        partNumber = "MAZ-SNS-SKY-112",
        serialNumber = "MZ9834521",
        description = "Sensor Skyactiv",
        provider = "Mazda",
        supervisorName = supervisorName,
        supervisorId = supervisorId,
        status = ProductStatus.ON_GOING
    )
    val mazdaHitos = templates.mapIndexed { i, name ->
        Milestone(name = name, order = i, plannedStart = getPastDate(10), plannedEnd = getPastDate(5), status = MilestoneStatus.PENDIENTE)
    }
    viewModel.registerFullProduct(mazda, mazdaHitos) {}

    // 4. Subaru - ON_GOING (Progreso 50%)
    val subaru = Product(
        classification = ProductClassification.NUEVO_PRODUCTO,
        partNumber = "SUB-INJ-BOX-778",
        serialNumber = "SB2026-5562",
        description = "Inyector 2.0L",
        provider = "Subaru",
        supervisorName = supervisorName,
        supervisorId = supervisorId,
        status = ProductStatus.ON_GOING
    )
    val subaruHitos = templates.mapIndexed { i, name ->
        if (i < templates.size / 2) {
            Milestone(name = name, order = i, plannedStart = getPastDate(20), plannedEnd = getPastDate(10), realStart = getPastDate(15), realEnd = getPastDate(11), status = MilestoneStatus.COMPLETADO)
        } else {
            Milestone(name = name, order = i, plannedStart = getFutureDate(i), plannedEnd = getFutureDate(i + 5), status = MilestoneStatus.PENDIENTE)
        }
    }
    viewModel.registerFullProduct(subaru, subaruHitos) {}

    // 5. Stellantis - APROBACION_FINAL
    val stellan = Product(
        classification = ProductClassification.NUEVO_PRODUCTO,
        partNumber = "STL-TRN-AUTO-991",
        serialNumber = "STL00456",
        description = "Transmisión Auto",
        provider = "Stellantis",
        supervisorName = supervisorName,
        supervisorId = supervisorId,
        status = ProductStatus.APROBACION_FINAL
    )
    val stellanHitos = templates.mapIndexed { i, name ->
        Milestone(name = name, order = i, plannedStart = getPastDate(30), plannedEnd = getPastDate(20), realStart = getPastDate(25), realEnd = getPastDate(21), status = MilestoneStatus.COMPLETADO)
    }
    viewModel.registerFullProduct(stellan, stellanHitos) {}
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
                    ProductStatus.PLANNING -> Color.Gray.copy(alpha = 0.1f)
                    ProductStatus.ON_GOING -> Color(0xFF007AFF).copy(alpha = 0.1f)
                    ProductStatus.COMPLETED -> Color(0xFF34C759).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                },
                contentColor = when(product.status) {
                    ProductStatus.PLANNING -> Color.Gray
                    ProductStatus.ON_GOING -> Color(0xFF007AFF)
                    ProductStatus.COMPLETED -> Color(0xFF34C759)
                    else -> MaterialTheme.colorScheme.tertiary
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
