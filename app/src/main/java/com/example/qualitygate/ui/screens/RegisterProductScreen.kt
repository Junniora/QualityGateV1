package com.example.qualitygate.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.qualitygate.data.model.ProductClassification
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onRegistrationSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    var partNumber by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("Toyota") }
    var classification by remember { mutableStateOf(ProductClassification.NUEVO_PRODUCTO) }
    
    var expandedProvider by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }
    
    val providers = listOf("Toyota", "Subaru", "Ford", "Mazda", "Stellantis")
    val selectedPhotos = remember { mutableStateListOf<Uri>() }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedPhotos.add(tempPhotoUri!!)
        }
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val registrationState by productViewModel.registrationState.collectAsState()

    val supervisorName = currentUser?.name ?: "Cargando..."
    val supervisorId = currentUser?.id ?: ""

    LaunchedEffect(registrationState) {
        registrationState?.getOrNull()?.let { productId ->
            onRegistrationSuccess(productId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Registro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    Text("Detalles del Producto", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)

                    OutlinedTextField(
                        value = partNumber,
                        onValueChange = { partNumber = it },
                        label = { Text("Número de Pieza") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Número de Serie") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    // Dropdown para Proveedor
                    ExposedDropdownMenuBox(
                        expanded = expandedProvider,
                        onExpandedChange = { expandedProvider = !expandedProvider }
                    ) {
                        OutlinedTextField(
                            value = provider,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Proveedor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvider) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProvider,
                            onDismissRequest = { expandedProvider = false }
                        ) {
                            providers.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        provider = p
                                        expandedProvider = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown para Clasificación
                    ExposedDropdownMenuBox(
                        expanded = expandedClass,
                        onExpandedChange = { expandedClass = !expandedClass }
                    ) {
                        OutlinedTextField(
                            value = classification.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Clasificación") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClass,
                            onDismissRequest = { expandedClass = false }
                        ) {
                            ProductClassification.entries.forEach { entry ->
                                DropdownMenuItem(
                                    text = { Text(entry.name.replace("_", " ")) },
                                    onClick = {
                                        classification = entry
                                        expandedClass = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Text("Fotografías (Opcional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                items(selectedPhotos) { uri ->
                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedPhotos.remove(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 12.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = {
                            val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                            tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            cameraLauncher.launch(tempPhotoUri!!)
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Text("Tomar Foto", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = { 
                        isUploading = true
                        productViewModel.registerProduct(
                            classification, partNumber, serialNumber, description, provider, supervisorName, supervisorId, selectedPhotos
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = partNumber.isNotBlank() && serialNumber.isNotBlank() && description.isNotBlank() && !isUploading
                ) {
                    Text("Registrar e Iniciar Planeación", fontWeight = FontWeight.SemiBold)
                }
            }

            registrationState?.exceptionOrNull()?.let {
                Text(text = "Error: ${it.message}", color = MaterialTheme.colorScheme.error)
                isUploading = false
            }
        }
    }
}
