package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.data.local.entity.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EmpleadosUiState(
    val mensaje: String? = null,
    val guardando: Boolean = false
)

class EmpleadosViewModel(
    private val deps: AppDependencies
) : ViewModel() {

    val empleados = deps.database.empleadoDao().observarTodos()

    private val _ui = MutableStateFlow(EmpleadosUiState())
    val ui: StateFlow<EmpleadosUiState> = _ui.asStateFlow()

    fun limpiarMensaje() {
        _ui.update { it.copy(mensaje = null) }
    }

    fun registrarRecolector(
        nombreCompleto: String,
        telefono: String,
        tipoContrato: TipoContrato,
        tarifaBase: Double,
        usuarioRecolector: String,
        contrasena: String
    ) {
        registrarConRol(
            nombreCompleto,
            telefono,
            tipoContrato,
            tarifaBase,
            usuarioRecolector,
            contrasena,
            RolUsuario.RECOLECTOR
        )
    }

    fun registrarConRol(
        nombreCompleto: String,
        telefono: String,
        tipoContrato: TipoContrato,
        tarifaBase: Double,
        usuario: String,
        contrasena: String,
        rol: RolUsuario
    ) {
        if (nombreCompleto.isBlank() || usuario.isBlank() || contrasena.isBlank()) {
            _ui.update { it.copy(mensaje = "Nombre, usuario y contraseña son obligatorios.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true, mensaje = null) }
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    val empleadoId = deps.database.empleadoDao().insertar(
                        Empleado(
                            nombreCompleto = nombreCompleto.trim(),
                            telefono = telefono.trim(),
                            tipoContrato = tipoContrato,
                            tarifaBase = tarifaBase,
                            activo = true
                        )
                    )
                    deps.database.usuarioDao().insertar(
                        Usuario(
                            usuario = usuario.trim(),
                            contrasena = contrasena,
                            rol = rol,
                            empleadoId = empleadoId
                        )
                    )
                }
            }
            _ui.update {
                it.copy(
                    guardando = false,
                    mensaje = resultado.exceptionOrNull()?.localizedMessage
                        ?: "${rol.name.lowercase().replaceFirstChar { it.uppercase() }} registrado correctamente."
                )
            }
        }
    }

    fun actualizarEmpleado(
        empleado: Empleado,
        nuevoUsuario: String?,
        nuevaContrasena: String?
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true, mensaje = null) }
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    deps.database.empleadoDao().actualizar(empleado)
                    val usuarioTexto = nuevoUsuario?.trim().orEmpty()
                    val claveTexto = nuevaContrasena.orEmpty()
                    if (usuarioTexto.isNotEmpty() && claveTexto.isNotEmpty()) {
                        deps.database.usuarioDao().actualizarCredencialesPorEmpleado(
                            empleadoId = empleado.id,
                            usuario = usuarioTexto,
                            contrasena = claveTexto
                        )
                    }
                }
            }
            _ui.update {
                it.copy(
                    guardando = false,
                    mensaje = resultado.exceptionOrNull()?.localizedMessage
                        ?: "Empleado actualizado."
                )
            }
        }
    }

    fun cambiarEstadoActivo(empleado: Empleado, activo: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deps.database.empleadoDao().establecerActivo(empleado.id, activo)
            }
        }
    }
}
