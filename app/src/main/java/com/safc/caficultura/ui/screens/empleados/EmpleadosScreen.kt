package com.safc.caficultura.ui.screens.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.EmpleadosViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadosRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val vm: EmpleadosViewModel = viewModel(factory = factory)
    val empleados by vm.empleados.collectAsStateWithLifecycle(initialValue = emptyList())
    val ui by vm.ui.collectAsStateWithLifecycle()
    val sesion by deps.sesion.collectAsStateWithLifecycle()
    val esSuperAdmin = sesion?.usuario?.rol == RolUsuario.SUPER_ADMIN

    var mostrarAlta by remember { mutableStateOf(false) }
    var empleadoEdicion by remember { mutableStateOf<Empleado?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Cabecera como en la imagen
        HeaderAdmin(
            titulo = "SAFC",
            subtitulo = "Administración",
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
                    Text("Gestión de Empleados", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Administre el personal de la finca", fontSize = 14.sp, color = Color.Gray)
                }
                Button(
                    onClick = { mostrarAlta = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo Empleado", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(empleados, key = { it.id }) { emp ->
                    TarjetaEmpleadoCafe(
                        empleado = emp,
                        onEdit = { empleadoEdicion = emp }
                    )
                }
            }
        }
    }

    if (mostrarAlta) {
        DialogoNuevoEmpleado(
            puedeAsignarRol = esSuperAdmin,
            cargando = ui.guardando,
            onDismiss = { mostrarAlta = false },
            onConfirmar = { nombre, tel, tipo, tarifa, user, pass, rol ->
                vm.registrarConRol(nombre, tel, tipo, tarifa, user, pass, rol)
                mostrarAlta = false
            }
        )
    }

    empleadoEdicion?.let { emp ->
        DialogoNuevoEmpleado(
            empleadoExistente = emp,
            cargando = ui.guardando,
            onDismiss = { empleadoEdicion = null },
            onConfirmar = { nombre, tel, tipo, tarifa, user, pass, _ ->
                vm.actualizarEmpleado(emp.copy(nombreCompleto = nombre, telefono = tel, tipoContrato = tipo, tarifaBase = tarifa), user, pass)
                empleadoEdicion = null
            }
        )
    }
}

@Composable
fun HeaderAdmin(titulo: String, subtitulo: String, onLogout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF004D40), // Verde oscuro de la imagen
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini logo
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MainGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("☕", color = Color.White, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(subtitulo, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir", tint = Color.White)
            }
        }
    }
}

@Composable
fun TarjetaEmpleadoCafe(empleado: Empleado, onEdit: () -> Unit) {
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    val tarifaTexto = if (empleado.tipoContrato == TipoContrato.PESO) {
        "Por peso • ${formatoMoneda.format(empleado.tarifaBase)}/kg"
    } else {
        "Por día • ${formatoMoneda.format(empleado.tarifaBase)}/día"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(empleado.nombreCompleto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.width(12.dp))
                    StatusBadge(activo = empleado.activo)
                }
                Text(tarifaTexto, fontSize = 14.sp, color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedIconButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                OutlinedIconButton(
                    onClick = { /* Eliminar logic */ },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(activo: Boolean) {
    val color = if (activo) MainGreen else Color(0xFF37474F)
    val texto = if (activo) "Activo" else "Inactivo"
    Surface(
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoNuevoEmpleado(
    empleadoExistente: Empleado? = null,
    puedeAsignarRol: Boolean = false,
    cargando: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: (String, String, TipoContrato, Double, String, String, RolUsuario) -> Unit
) {
    var nombre by remember { mutableStateOf(empleadoExistente?.nombreCompleto ?: "") }
    var tel by remember { mutableStateOf(empleadoExistente?.telefono ?: "") }
    var tipo by remember { mutableStateOf(empleadoExistente?.tipoContrato ?: TipoContrato.PESO) }
    var tarifa by remember { mutableStateOf(empleadoExistente?.tarifaBase?.toString() ?: "2500") }
    var usuario by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(RolUsuario.RECOLECTOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(if (empleadoExistente == null) "Registrar Nuevo Empleado" else "Editar Empleado", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = tel, onValueChange = { tel = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                
                Column {
                    Text("Tipo de Contrato", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tipo == TipoContrato.PESO,
                            onClick = { tipo = TipoContrato.PESO },
                            label = { Text("Por Peso") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tipo == TipoContrato.DIA,
                            onClick = { tipo = TipoContrato.DIA },
                            label = { Text("Por Día") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(value = tarifa, onValueChange = { tarifa = it }, label = { Text("Salario Base / Tarifa") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                
                if (empleadoExistente == null) {
                    Text("Credenciales de Acceso", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    if (puedeAsignarRol) {
                        Column {
                            Text("Rol del Usuario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = rol == RolUsuario.ADMIN,
                                    onClick = { rol = RolUsuario.ADMIN },
                                    label = { Text("Admin") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = rol == RolUsuario.RECOLECTOR,
                                    onClick = { rol = RolUsuario.RECOLECTOR },
                                    label = { Text("Recolector") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(value = usuario, onValueChange = { usuario = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                Button(
                    onClick = { onConfirmar(nombre, tel, tipo, tarifa.toDoubleOrNull() ?: 0.0, usuario, pass, rol) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
                ) {
                    Text(if (empleadoExistente == null) "Guardar Empleado" else "Actualizar Empleado", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
