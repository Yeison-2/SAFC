package com.safc.caficultura.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.ui.navigation.Rutas
import com.safc.caficultura.ui.theme.BackgroundGreen
import com.safc.caficultura.ui.theme.DarkGreenText
import com.safc.caficultura.ui.theme.MainGreen
import com.safc.caficultura.ui.viewmodel.MenuViewModel
import com.safc.caficultura.ui.viewmodel.SafcViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MenuRoute(
    navController: NavHostController,
    deps: AppDependencies,
    factory: SafcViewModelFactory
) {
    val sesion by deps.sesion.collectAsStateWithLifecycle()
    val vm: MenuViewModel = viewModel(factory = factory)
    val kgHoy by vm.sumaKgHoy.collectAsStateWithLifecycle(initialValue = 0.0)
    val jornalesHoy by vm.sumaJornalesHoy.collectAsStateWithLifecycle(initialValue = 0.0)
    val pagosHoy by vm.sumaPagosHoy.collectAsStateWithLifecycle(initialValue = 0.0)

    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val rol = sesion?.usuario?.rol
    val esAdmin = rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGreen)
    ) {
        HeaderDashboard(
            nombre = sesion?.usuario?.usuario ?: "Usuario",
            rol = when (sesion?.usuario?.rol) {
                RolUsuario.SUPER_ADMIN -> "Super Administrador"
                RolUsuario.ADMIN -> "Administrador"
                else -> "Recolector"
            },
            telefono = sesion?.empleado?.telefono,
            onLogout = {
                deps.cerrarSesion()
                navController.navigate(Rutas.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(
                text = "Resumen de hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003324)
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResumenCard(
                    titulo = "Recolección",
                    valor = if (kgHoy % 1.0 == 0.0) "${kgHoy.toInt()} kg" else "${String.format(Locale.getDefault(), "%.1f", kgHoy)} kg",
                    color = MainGreen,
                    modifier = Modifier.weight(1f)
                )
                if (esAdmin) {
                    ResumenCard(
                        titulo = "Pagos Hoy",
                        valor = formatoMoneda.format(pagosHoy),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ResumenCard(
                        titulo = "Jornales",
                        valor = "${jornalesHoy.toInt()} día(s)",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!esAdmin) {
                Spacer(Modifier.height(16.dp))
                ResumenCard(
                    titulo = "Pagos Recibidos",
                    valor = formatoMoneda.format(pagosHoy),
                    color = Color(0xFFFFA000),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003324)
            )
            Spacer(Modifier.height(16.dp))

            AccesoRapidoItem("Ver Producción", "Revisa tus registros diarios", { navController.navigate(Rutas.Produccion) })
            Spacer(Modifier.height(12.dp))
            AccesoRapidoItem("Estado de Pagos", "Consulta tus saldos pendientes", { navController.navigate(Rutas.Pagos) })
            Spacer(Modifier.height(12.dp))
            AccesoRapidoItem("Ver Reportes", "Análisis de desempeño y costos", { navController.navigate(Rutas.Reportes) })
            
            if (!esAdmin) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Mi Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF003324)
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MainGreen.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MainGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(text = sesion?.empleado?.nombreCompleto ?: "Nombre no disponible", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(text = "📞 ${sesion?.empleado?.telefono ?: "Sin teléfono"}", color = Color.Gray, fontSize = 14.sp)
                        Text(text = "📄 Contrato: ${if (sesion?.empleado?.tipoContrato == TipoContrato.PESO) "Por Peso" else "Por Día"}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            val rolActual = sesion?.usuario?.rol
            if (rolActual == RolUsuario.ADMIN || rolActual == RolUsuario.SUPER_ADMIN) {
                Spacer(Modifier.height(12.dp))
                AccesoRapidoItem("Gestionar Empleados", "Altas y bajas de personal", { navController.navigate(Rutas.Empleados) })
            }
        }
    }
}

@Composable
fun HeaderDashboard(
    nombre: String, 
    rol: String, 
    telefono: String? = null,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Hola, $nombre",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003324)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rol,
                    fontSize = 14.sp,
                    color = Color(0xFF003324).copy(alpha = 0.6f)
                )
                if (!telefono.isNullOrBlank()) {
                    Text(
                        text = " • $telefono",
                        fontSize = 14.sp,
                        color = Color(0xFF003324).copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ResumenCard(titulo: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = titulo, fontSize = 12.sp, color = Color.Black.copy(alpha = 0.6f))
            Text(text = valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun AccesoRapidoItem(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5FBF7)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MainGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = MainGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitulo, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
