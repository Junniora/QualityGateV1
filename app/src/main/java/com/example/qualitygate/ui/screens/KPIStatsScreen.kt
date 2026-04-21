package com.example.qualitygate.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qualitygate.data.model.Product
import com.example.qualitygate.data.model.ProductStatus
import com.example.qualitygate.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KPIStatsScreen(productViewModel: ProductViewModel) {
    val products by productViewModel.productList.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("KPI Dashboard", fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Resumen de Totales estilo iOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total de Proyectos", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    Text("${products.size}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Gráfica de Pastel (Distribución por Fase)
            KPIChartCard(title = "Distribución por Fase", icon = Icons.Default.PieChart) {
                PhasePieChart(products)
            }

            // Gráfica de Barras (Registros por Usuario)
            KPIChartCard(title = "Líderes de Registro", icon = Icons.Default.BarChart) {
                UserBarChart(products)
            }
        }
    }
}

@Composable
fun KPIChartCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

@Composable
fun PhasePieChart(products: List<Product>) {
    if (products.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos", color = Color.Gray)
        }
        return
    }

    val stats = products.groupBy { it.status }.mapValues { it.value.size }
    val colors = listOf(Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFF34C759), Color(0xFFFF9500), Color(0xFFFF3B30), Color(0xFFAF52DE))
    
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val total = products.size.toFloat()
                
                stats.values.forEachIndexed { index, count ->
                    val sweep = (count / total) * 360f
                    drawArc(
                        color = colors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 25.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }
            Text("${products.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.width(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            stats.keys.forEachIndexed { index, status ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(colors.getOrElse(index) { Color.Gray }, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("${status.name}: ${stats[status]}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun UserBarChart(products: List<Product>) {
    val userStats = products.groupBy { it.supervisorName }.mapValues { it.value.size }
        .toList().sortedByDescending { it.second }.take(5)

    if (userStats.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos", color = Color.Gray)
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        userStats.forEach { (name, count) ->
            val max = userStats.firstOrNull()?.second ?: 1
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text("$count", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { count.toFloat() / max.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
