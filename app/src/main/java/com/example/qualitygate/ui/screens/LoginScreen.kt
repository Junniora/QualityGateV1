package com.example.qualitygate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualitygate.R
import com.example.qualitygate.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel, 
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState?.isSuccess == true) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo (Si no existe aún, se puede comentar esta parte o usar un icono temporal)
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "QualityGate Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "QualityGate", 
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Gestión de Calidad Automotriz", 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(
                "¿No tienes cuenta? Regístrate aquí", 
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (authState?.isFailure == true) {
            Text(
                text = authState?.exceptionOrNull()?.message ?: "Error desconocido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
