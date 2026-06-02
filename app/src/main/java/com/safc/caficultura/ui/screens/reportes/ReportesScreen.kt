package com.safc.caficultura.ui.screens.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.entity.EstadoPago
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.ui.screens.empleados.HeaderAdmin
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.ReportesViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory
import com.safc.caficultura.util.CafeUnits
import com.safc.caficultura.util.PdfReportGenerator
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val context = LocalContext.current
    val vm: ReportesViewModel = viewModel(factory = factory)
    val pagos by vm.pagosVisibles.collectAsStateWithLifecycle()
    val sesion by deps.sesion.collectAsStateWithLifecycle()
    val rol = sesion?.usuario?.rol
    val esAdmin = rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN

    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    val pagosValidos = pagos.filter { it.pago.estado != EstadoPago.CANCELADO }
    
    val totalKg = pagosValidos
        .filter { it.empleado.tipoContrato == TipoContrato.PESO }
        .sumOf { it.pago.totalCantidad }
        
    val totalJornales = pagosValidos
        .filter { it.empleado.tipoContrato == TipoContrato.DIA }
        .sumOf { it.pago.totalCantidad }

    val costoTotal = pagosValidos.sumOf { it.pago.montoTotal }
    val promedio = if (pagosValidos.isNotEmpty()) totalKg / pagosValidos.size else 0.0

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        HeaderAdmin(
            titulo = "SAFC",
            subtitulo = if (esAdmin) "Administración" else "Mi Desempeño",
            onLogout = {
                deps.cerrarSesion()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            }
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (esAdmin) "Reportes y Estadísticas" else "Mi Resumen de Actividad", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(if (esAdmin) "Análisis de producción y rendimiento" else "Consulta tu progreso y ganancias acumuladas", fontSize = 14.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { 
                            PdfReportGenerator.generarYCompartirReporte(
                                context,
                                pagosValidos,
                                totalKg,
                                totalJornales,
                                costoTotal
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Exportar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Periodo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = "Último mes",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tipo de Reporte", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = "General",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                }
            }

            item {
                ReporteCard(
                    titulo = "Producción Total (Mes)",
                    valor = "${String.format(Locale.getDefault(), "%,.0f", totalKg)} kg",
                    subvalor = "${totalJornales.toInt()} jornadas reportadas",
                    color = MainGreen
                )
            }

            item {
                ReporteCard(
                    titulo = "Promedio por Empleado",
                    valor = "${String.format(Locale.getDefault(), "%,.0f", promedio)} kg",
                    cambio = "+5%",
                    color = MainGreen
                )
            }

            item {
                ReporteCard(
                    titulo = "Costos Laborales",
                    valor = formatoMoneda.format(costoTotal),
                    cambio = "+8%",
                    color = MainGreen
                )
            }

            item {
                ReporteCard(
                    titulo = "Lote Más Productivo",
                    valor = "Lote A",
                    subvalor = "890 kg (71.2 @)",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ReporteCard(titulo: String, valor: String, subvalor: String? = null, cambio: String? = null, color: Color = MainGreen) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(titulo, fontSize = 13.sp, color = Color.Gray)
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            if (subvalor != null) {
                Text(subvalor, fontSize = 13.sp, color = Color.Gray)
            }
            if (cambio != null) {
                Text(cambio, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}
