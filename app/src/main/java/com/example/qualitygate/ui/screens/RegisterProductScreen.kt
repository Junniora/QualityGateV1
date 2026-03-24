package com.example.qualitygate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualitygate.data.model.ProductClassification
import com.example.qualitygate.ui.viewmodel.AuthViewModel
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onRegistrationSuccess: (String) -> Unit
) {
    var partNumber by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf(ProductClassification.NUEVO_PRODUCTO) }
    var expanded by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val registrationState by productViewModel.registrationState.collectAsState()

    val supervisorName = currentUser?.name ?: "Cargando..."

    LaunchedEffect(registrationState) {
        registrationState?.getOrNull()?.let { productId ->
            onRegistrationSuccess(productId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo Registro",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                Text("Detalles del Producto", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)

                OutlinedTextField(
                    value = partNumber,
                    onValueChange = { partNumber = it },
                    label = { Text("Número de Pieza") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                OutlinedTextField(
                    value = supervisorName,
                    onValueChange = {},
                    label = { Text("Responsable") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = classification.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clasificación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ProductClassification.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.name.replace("_", " ")) },
                                onClick = {
                                    classification = entry
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                productViewModel.registerProduct(classification, partNumber, supervisorName) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = partNumber.isNotBlank() && currentUser != null
        ) {
            Text("Crear Producto", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        }

        AnimatedVisibility(
            visible = registrationState?.isFailure == true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            registrationState?.exceptionOrNull()?.let {
                Text(
                    text = "Error: ${it.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
