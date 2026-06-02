package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.dao.ProduccionConEmpleado
import com.safc.caficultura.data.local.entity.Produccion
import com.safc.caficultura.data.local.entity.UnidadMedida
import com.safc.caficultura.util.CafeUnits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProduccionUiState(
    val mensaje: String? = null,
    val guardando: Boolean = false
)

class ProduccionViewModel(
    private val deps: AppDependencies
) : ViewModel() {

    private val _ui = MutableStateFlow(ProduccionUiState())
    val ui: StateFlow<ProduccionUiState> = _ui.asStateFlow()

    fun produccionesVisibles(): Flow<List<ProduccionConEmpleado>> =
        deps.sesion.flatMapLatest { detalle ->
            val rol = detalle?.usuario?.rol
            when {
                detalle == null -> flowOf(emptyList())
                rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN ->
                    deps.database.produccionDao().observarTodasConEmpleado()

                else -> {
                    val empleadoId = detalle.usuario.empleadoId
                    if (empleadoId == null) flowOf(emptyList())
                    else deps.database.produccionDao().observarPorEmpleado(empleadoId)
                }
            }
        }

    fun limpiarMensaje() {
        _ui.update { it.copy(mensaje = null) }
    }

    fun registrarProduccion(
        empleadoId: Long,
        fechaEpochDay: Long,
        valorTexto: String,
        unidad: UnidadMedida,
        id: Long = 0
    ) {
        if (unidad == UnidadMedida.JORNAL) {
            registrarJornal(empleadoId, fechaEpochDay, id)
            return
        }
        val valorOriginal = if (unidad == UnidadMedida.ARROBA) {
            CafeUnits.parseArrobas(valorTexto)
        } else {
            valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
        }

        if (valorOriginal <= 0.0) {
            _ui.update { it.copy(mensaje = "Indique un valor válido mayor que cero.") }
            return
        }

        val valorEnKg = if (unidad == UnidadMedida.ARROBA) {
            CafeUnits.arrobasAKg(valorOriginal)
        } else {
            valorOriginal
        }

        viewModelScope.launch {
            _ui.update { it.copy(guardando = true, mensaje = null) }
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    deps.database.produccionDao().insertar(
                        Produccion(
                            id = id,
                            empleadoId = empleadoId,
                            fecha = fechaEpochDay,
                            valor = valorEnKg,
                            valorOriginal = valorOriginal,
                            unidad = unidad
                        )
                    )
                }
            }
            _ui.update {
                it.copy(
                    guardando = false,
                    mensaje = resultado.exceptionOrNull()?.localizedMessage
                        ?: if (id == 0L) "Producción registrada." else "Producción actualizada."
                )
            }
        }
    }

    private fun registrarJornal(empleadoId: Long, fechaEpochDay: Long, id: Long = 0) {
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true, mensaje = null) }
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    deps.database.produccionDao().insertar(
                        Produccion(
                            id = id,
                            empleadoId = empleadoId,
                            fecha = fechaEpochDay,
                            valor = 1.0, // Representa 1 día de trabajo
                            valorOriginal = 1.0,
                            unidad = UnidadMedida.JORNAL
                        )
                    )
                }
            }
            _ui.update {
                it.copy(
                    guardando = false,
                    mensaje = resultado.exceptionOrNull()?.localizedMessage 
                        ?: if (id == 0L) "Día de trabajo reportado." else "Registro actualizado."
                )
            }
        }
    }

    fun eliminar(item: ProduccionConEmpleado) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deps.database.produccionDao().eliminar(item.produccion)
            }
        }
    }
}
