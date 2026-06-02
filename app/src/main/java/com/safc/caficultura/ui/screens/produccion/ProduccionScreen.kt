package com.safc.caficultura.ui.screens.produccion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.dao.ProduccionConEmpleado
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.data.local.entity.UnidadMedida
import com.safc.caficultura.ui.screens.empleados.HeaderAdmin
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.ProduccionViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory
import com.safc.caficultura.util.CafeUnits
import com.safc.caficultura.util.Fechas
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduccionRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val vm: ProduccionViewModel = viewModel(factory = factory)
    val ui by vm.ui.collectAsStateWithLifecycle()
    val sesion by deps.sesion.collectAsStateWithLifecycle()
    val rol = sesion?.usuario?.rol
    val esAdmin = rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN

    val snackbarHostState = remember { SnackbarHostState() }

    var producciones by remember { mutableStateOf<List<ProduccionConEmpleado>>(emptyList()) }
    LaunchedEffect(vm, sesion) {
        vm.produccionesVisibles().collectLatest { producciones = it }
    }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    var mostrarRegistro by remember { mutableStateOf(false) }
    var produccionAEditar by remember { mutableStateOf<ProduccionConEmpleado?>(null) }
    var produccionAEliminar by remember { mutableStateOf<ProduccionConEmpleado?>(null) }

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
                subtitulo = if (esAdmin) "Administración" else "Mi Producción",
                onLogout = {
                    deps.cerrarSesion()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestión de Producción", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Registre y consulte la producción diaria", fontSize = 14.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { mostrarRegistro = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nuevo Registro", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Producción\nReciente", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp)
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text("Buscar...", fontSize = 14.sp) },
                                modifier = Modifier.width(160.dp).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray)
                            )
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(producciones, key = { it.produccion.id }) { item ->
                                FilaProduccionCafe(
                                    item = item,
                                    puedeGestionar = esAdmin,
                                    onEdit = { produccionAEditar = item },
                                    onDelete = { produccionAEliminar = item }
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarRegistro) {
        DialogoRegistrarProduccion(
            esAdmin = esAdmin,
            deps = deps,
            onDismiss = { mostrarRegistro = false },
            onConfirmar = { empId, fecha, valor, unidad ->
                vm.registrarProduccion(empId, fecha, valor, unidad)
                mostrarRegistro = false
            }
        )
    }

    produccionAEditar?.let { item ->
        DialogoRegistrarProduccion(
            esAdmin = esAdmin,
            deps = deps,
            produccionExistente = item,
            onDismiss = { produccionAEditar = null },
            onConfirmar = { empId, fecha, valor, unidad ->
                vm.registrarProduccion(empId, fecha, valor, unidad, id = item.produccion.id)
                produccionAEditar = null
            }
        )
    }

    produccionAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { produccionAEliminar = null },
            title = { Text("Eliminar Registro") },
            text = { Text("¿Está seguro que desea eliminar este registro de producción? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.eliminar(item)
                        produccionAEliminar = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { produccionAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FilaProduccionCafe(
    item: ProduccionConEmpleado,
    puedeGestionar: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val valorTexto = when (item.produccion.unidad) {
        UnidadMedida.KG -> "${item.produccion.valorOriginal} kg"
        UnidadMedida.ARROBA -> CafeUnits.formatearArrobas(item.produccion.valorOriginal)
        UnidadMedida.JORNAL -> "1 Día Lab."
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.empleado.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Lote A • ${Fechas.formatear(item.produccion.fecha)}", 
                fontSize = 13.sp, 
                color = Color.Gray
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = valorTexto,
                fontWeight = FontWeight.ExtraBold,
                color = MainGreen,
                fontSize = 18.sp
            )
            
            if (puedeGestionar) {
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRegistrarProduccion(
    esAdmin: Boolean,
    deps: AppDependencies,
    produccionExistente: ProduccionConEmpleado? = null,
    onDismiss: () -> Unit,
    onConfirmar: (Long, Long, String, UnidadMedida) -> Unit
) {
    val empleadosActivos by deps.database.empleadoDao().observarActivos()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val sesion by deps.sesion.collectAsStateWithLifecycle()

    var empleadoSeleccionado by remember { 
        mutableStateOf(produccionExistente?.empleado ?: empleadosActivos.find { it.id == produccionExistente?.produccion?.empleadoId } ?: empleadosActivos.firstOrNull()) 
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = produccionExistente?.produccion?.fecha?.let { it * 86400000L } ?: System.currentTimeMillis()
    )
    var mostrarDatePicker by remember { mutableStateOf(false) }
    
    val fechaSeleccionada = datePickerState.selectedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
    } ?: LocalDate.now()

    var valorTexto by remember { mutableStateOf(produccionExistente?.produccion?.valorOriginal?.toString() ?: "") }
    var unidad by remember { mutableStateOf(produccionExistente?.produccion?.unidad ?: UnidadMedida.KG) }

    val emp = empleadoSeleccionado
    val esPorDia = emp?.tipoContrato == TipoContrato.DIA
    
    LaunchedEffect(emp) {
        if (esPorDia) {
            unidad = UnidadMedida.JORNAL
        } else if (unidad == UnidadMedida.JORNAL) {
            unidad = UnidadMedida.KG
        }
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("OK", color = MainGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    todayContentColor = MainGreen,
                    todayDateBorderColor = MainGreen,
                    selectedDayContainerColor = MainGreen,
                    selectedDayContentColor = Color.White
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(if (produccionExistente == null) "Nuevo Registro" else "Editar Registro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (esAdmin) {
                    var expandido by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                        OutlinedTextField(
                            value = empleadoSeleccionado?.nombreCompleto ?: "Seleccione empleado",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Empleado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            val listaAMostrar = if (produccionExistente != null) {
                                (empleadosActivos + listOfNotNull(produccionExistente.empleado)).distinctBy { it.id }
                            } else {
                                empleadosActivos
                            }
                            listaAMostrar.forEach { empItem ->
                                DropdownMenuItem(text = { Text(empItem.nombreCompleto) }, onClick = {
                                    empleadoSeleccionado = empItem
                                    expandido = false
                                })
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = Fechas.formatear(fechaSeleccionada.toEpochDay()),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { mostrarDatePicker = true }
                    )
                }
                
                if (!esPorDia) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = valorTexto, 
                            onValueChange = { valorTexto = it }, 
                            label = { Text("Cantidad") }, 
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilterChip(
                            selected = unidad == UnidadMedida.KG,
                            onClick = { unidad = UnidadMedida.KG },
                            label = { Text("kg") }
                        )
                        FilterChip(
                            selected = unidad == UnidadMedida.ARROBA,
                            onClick = { unidad = UnidadMedida.ARROBA },
                            label = { Text("@") }
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MainGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reportando: ", fontWeight = FontWeight.Bold)
                            Text("1 Día de trabajo (Jornal)", color = MainGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        val id = if (esAdmin) empleadoSeleccionado?.id else sesion?.usuario?.empleadoId
                        if (id != null) {
                            onConfirmar(id, fechaSeleccionada.toEpochDay(), if (esPorDia) "1" else valorTexto, unidad)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
                ) {
                    Text(
                        text = when {
                            produccionExistente != null -> "Actualizar Registro"
                            esPorDia -> "Confirmar Trabajo"
                            else -> "Guardar Pesaje"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
