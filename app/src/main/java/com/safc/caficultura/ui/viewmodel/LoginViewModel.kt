package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val navegarAlMenu: Boolean = false
)

class LoginViewModel(
    private val deps: AppDependencies
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun iniciarSesion(usuario: String, contrasena: String) {
        if (usuario.isBlank() || contrasena.isBlank()) {
            _ui.update { it.copy(mensajeError = "Complete usuario y contraseña.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, mensajeError = null, navegarAlMenu = false) }
            val resultado = withContext(Dispatchers.IO) {
                deps.database.usuarioDao().autenticar(usuario.trim(), contrasena)
            }
            if (resultado == null) {
                _ui.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "Credenciales incorrectas.",
                        navegarAlMenu = false
                    )
                }
                return@launch
            }
            if (resultado.usuario.rol == RolUsuario.RECOLECTOR && resultado.usuario.empleadoId == null) {
                _ui.update {
                    it.copy(
                        cargando = false,
                        mensajeError = "El recolector no tiene empleado asociado. Contacte al administrador.",
                        navegarAlMenu = false
                    )
                }
                return@launch
            }
            deps.establecerSesion(resultado)

            // Enviar correo de inicio de sesión de forma asíncrona
            viewModelScope.launch {
                deps.mailConfig.sendEmail(
                    to = "huertasdayanna362@gmail.com",
                    subject = "Notificación de Inicio de Sesión - Caficultura",
                    body = """
                        Hola,
                        
                        Se ha detectado un inicio de sesión en la aplicación Caficultura.
                        
                        Usuario: ${resultado.usuario.usuario}
                        Rol: ${resultado.usuario.rol}
                        Fecha/Hora: ${java.util.Date()}
                        
                        Saludos,
                        Sistema SAFC
                    """.trimIndent()
                )
            }

            _ui.update { it.copy(cargando = false, mensajeError = null, navegarAlMenu = true) }
        }
    }

    fun consumirNavegacion() {
        _ui.update { it.copy(navegarAlMenu = false) }
    }
}
