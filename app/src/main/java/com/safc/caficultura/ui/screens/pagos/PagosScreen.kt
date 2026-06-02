package com.safc.caficultura.ui.screens.pagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.dao.PagoConEmpleado
import com.safc.caficultura.data.local.entity.EstadoPago
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.ui.screens.empleados.HeaderAdmin
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.PagosViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory
import com.safc.caficultura.util.Fechas
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun PagosRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val vm: PagosViewModel = viewModel(factory = factory)
    val ui by vm.ui.collectAsStateWithLifecycle()
    val pagos by vm.pagosConEmpleado.collectAsStateWithLifecycle(initialValue = emptyList())
    val saldoPendiente by vm.saldoPendiente.collectAsStateWithLifecycle(initialValue = 0.0)
    val sesion by deps.sesion.collectAsStateWithLifecycle()
    val rol = sesion?.usuario?.rol
    val esAdmin = rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN

    var desdeTexto by remember { mutableStateOf(Fechas.formatear(LocalDate.now().minusDays(7).toEpochDay())) }
    var hastaTexto by remember { mutableStateOf(Fechas.formatear(LocalDate.now().toEpochDay())) }
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color(0xFFF5F5F5))
        ) {
            HeaderAdmin(
                titulo = "SAFC",
                subtitulo = if (esAdmin) "Administración" else "Mis Pagos",
                onLogout = {
                    deps.cerrarSesion()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )

            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
            ) {
                item {
                    Text(if (esAdmin) "Gestión de Pagos" else "Resumen de Pagos", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(if (esAdmin) "Calcule y administre los pagos de empleados" else "Consulta tu producción acumulada y pagos realizados", fontSize = 14.sp, color = Color.Gray)
                }

                if (!esAdmin) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MainGreen,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Por Liquidar", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Text(
                                    text = formatoMoneda.format(saldoPendiente),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text("Basado en tu recolección desde el último pago", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (esAdmin) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Calcular Pago", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Genere el pago basado en la producción registrada", fontSize = 14.sp, color = Color.Gray)
                                
                                Spacer(Modifier.height(20.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = desdeTexto, onValueChange = { desdeTexto = it }, label = { Text("Inicio") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                    OutlinedTextField(value = hastaTexto, onValueChange = { hastaTexto = it }, label = { Text("Fin") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                                }
                                
                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val d = Fechas.parseIso(desdeTexto).getOrNull()?.let { Fechas.aEpochDay(it) } ?: 0L
                                        val h = Fechas.parseIso(hastaTexto).getOrNull()?.let { Fechas.aEpochDay(it) } ?: 0L
                                        vm.generarPagos(d, h)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = !ui.calculando,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                                ) {
                                    if (ui.calculando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    else Text("Calcular y Generar Pago", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Historial de Pagos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (esAdmin) {
                            OutlinedTextField(
                                value = ui.queryBusqueda,
                                onValueChange = { vm.actualizarBusqueda(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Buscar empleado...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MainGreen) },
                                trailingIcon = {
                                    if (ui.queryBusqueda.isNotEmpty()) {
                                        IconButton(onClick = { vm.actualizarBusqueda("") }) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Limpiar", tint = Color.Gray)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MainGreen,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                        }
                    }
                }

                if (pagos.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (ui.queryBusqueda.isEmpty()) "No hay pagos registrados" else "No se encontraron resultados para '${ui.queryBusqueda}'",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(pagos, key = { it.pago.id }) { pago ->
                    FilaPagoCafe(
                        pago = pago, 
                        formatoMoneda = formatoMoneda,
                        puedeGestionar = esAdmin,
                        onCambiarEstado = { estado -> vm.cambiarEstadoPago(pago.pago.id, estado) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilaPagoCafe(
    pago: PagoConEmpleado, 
    formatoMoneda: NumberFormat,
    puedeGestionar: Boolean,
    onCambiarEstado: (EstadoPago) -> Unit
) {
    val cantidadTexto = if (pago.empleado.tipoContrato == TipoContrato.PESO) {
        "${pago.pago.totalCantidad.toInt()} kg"
    } else {
        "${pago.pago.totalCantidad.toInt()} días"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pago.empleado.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${Fechas.formatear(pago.pago.periodoInicio)} - ${Fechas.formatear(pago.pago.periodoFin)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(cantidadTexto, fontSize = 12.sp, color = MainGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatoMoneda.format(pago.pago.montoTotal),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFA000),
                        fontSize = 18.sp
                    )
                    StatusBadgePago(estado = pago.pago.estado)
                }
            }
            
            if (puedeGestionar && pago.pago.estado == EstadoPago.PENDIENTE) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onCambiarEstado(EstadoPago.PAGADO) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Pagar Ahora", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatusBadgePago(estado: EstadoPago) {
    val color = when(estado) {
        EstadoPago.PAGADO -> Color(0xFFE8F5E9)
        EstadoPago.CANCELADO -> Color(0xFFFFEBEE)
        EstadoPago.PENDIENTE -> Color(0xFFFFF3E0)
    }
    val textColor = when(estado) {
        EstadoPago.PAGADO -> Color(0xFF4CAF50)
        EstadoPago.CANCELADO -> Color(0xFFD32F2F)
        EstadoPago.PENDIENTE -> Color(0xFFFFA000)
    }
    val texto = when(estado) {
        EstadoPago.PAGADO -> "Pagado"
        EstadoPago.CANCELADO -> "Cancelado"
        EstadoPago.PENDIENTE -> "Pendiente"
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
