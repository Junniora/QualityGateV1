package com.example.qualitygate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qualitygate.data.model.UserRole
import com.example.qualitygate.ui.viewmodel.AuthViewModel

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onLogout: () -> Unit
) {
    val user = authViewModel.currentUser.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "QA PMS Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Bienvenido, ${user?.name ?: "Usuario"}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Rol: ${user?.role}", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(32.dp))

        when (user?.role) {
            UserRole.SUPERVISOR -> {
                Button(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar Nuevo Producto")
                }
            }
            UserRole.REVISOR -> {
                Button(onClick = onNavigateToProductList, modifier = Modifier.fillMaxWidth()) {
                    Text("Revisar Productos Pendientes")
                }
            }
            UserRole.APROBADOR -> {
                Button(onClick = onNavigateToProductList, modifier = Modifier.fillMaxWidth()) {
                    Text("Aprobaciones Finales")
                }
            }
            null -> {
                CircularProgressIndicator()
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToProductList, 
            modifier = Modifier.fillMaxWidth(), 
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Ver Lista de Productos")
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = onLogout) {
            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
        }
    }
}
