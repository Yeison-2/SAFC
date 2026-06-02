package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.entity.EstadoPago
import com.safc.caficultura.data.local.entity.Pago
import com.safc.caficultura.data.local.entity.TipoContrato
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class PagosUiState(
    val mensaje: String? = null,
    val calculando: Boolean = false,
    val queryBusqueda: String = ""
)

class PagosViewModel(
    private val deps: AppDependencies
) : ViewModel() {

    private val _ui = MutableStateFlow(PagosUiState())
    val ui: StateFlow<PagosUiState> = _ui.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagosConEmpleado = combine(
        deps.sesion.flatMapLatest { sesion ->
            if (sesion?.usuario?.rol == RolUsuario.ADMIN || sesion?.usuario?.rol == RolUsuario.SUPER_ADMIN) {
                deps.database.pagoDao().observarTodosConEmpleado()
            } else {
                val empId = sesion?.usuario?.empleadoId ?: -1L
                deps.database.pagoDao().observarPorEmpleado(empId)
            }
        },
        _ui.map { it.queryBusqueda }.distinctUntilChanged()
    ) { lista, query ->
        if (query.isBlank()) {
            lista
        } else {
            val regex = try {
                Regex(query, RegexOption.IGNORE_CASE)
            } catch (e: Exception) {
                null
            }
            if (regex != null) {
                lista.filter { regex.containsMatchIn(it.empleado.nombreCompleto) }
            } else {
                lista.filter { it.empleado.nombreCompleto.contains(query, ignoreCase = true) }
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _ui.update { it.copy(queryBusqueda = query) }
    }

    fun cambiarEstadoPago(pagoId: Long, nuevoEstado: EstadoPago) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deps.database.pagoDao().actualizarEstado(pagoId, nuevoEstado)
            }
            _ui.update { it.copy(mensaje = "Estado actualizado a ${nuevoEstado.name.lowercase()}") }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val saldoPendiente = deps.sesion.flatMapLatest { sesion ->
        val empId = sesion?.usuario?.empleadoId
        if (empId == null) flowOf(0.0)
        else {
            flow {
                val totalKg = deps.database.produccionDao().sumarKgEnRango(empId, LocalDate.now().withDayOfMonth(1).toEpochDay(), LocalDate.now().toEpochDay())
                val dias = deps.database.produccionDao().contarDiasEnRango(empId, LocalDate.now().withDayOfMonth(1).toEpochDay(), LocalDate.now().toEpochDay())
                val empleado = deps.database.empleadoDao().obtenerPorId(empId)
                
                val cantidad = if (empleado?.tipoContrato == TipoContrato.PESO) totalKg else dias.toDouble()
                emit(cantidad * (empleado?.tarifaBase ?: 0.0))
            }
        }
    }

    fun limpiarMensaje() {
        _ui.update { it.copy(mensaje = null) }
    }

    fun generarPagos(periodoInicioEpochDay: Long, periodoFinEpochDay: Long) {
        if (_ui.value.calculando) return

        if (periodoFinEpochDay < periodoInicioEpochDay) {
            _ui.update { it.copy(mensaje = "La fecha fin debe ser igual o posterior a la fecha inicio.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(calculando = true, mensaje = null) }
            val resultado = withContext(Dispatchers.IO) {
                runCatching {
                    val empleados = deps.database.empleadoDao().listarActivos()
                    val produccionDao = deps.database.produccionDao()
                    val pagoDao = deps.database.pagoDao()
                    var generados = 0
                    
                    for (empleado in empleados) {
                        // Evitar duplicados: Doble verificación (lógica + restricción DB)
                        if (pagoDao.existePagoEnPeriodo(empleado.id, periodoInicioEpochDay, periodoFinEpochDay)) {
                            continue
                        }

                        val cantidad: Double
                        val monto: Double
                        
                        if (empleado.tipoContrato == TipoContrato.PESO) {
                            cantidad = produccionDao.sumarKgEnRango(empleado.id, periodoInicioEpochDay, periodoFinEpochDay)
                            if (cantidad <= 0.0) continue
                            monto = cantidad * empleado.tarifaBase
                        } else {
                            val dias = produccionDao.contarDiasEnRango(empleado.id, periodoInicioEpochDay, periodoFinEpochDay)
                            if (dias <= 0) continue
                            cantidad = dias.toDouble()
                            monto = cantidad * empleado.tarifaBase
                        }

                        pagoDao.insertar(
                            Pago(
                                empleadoId = empleado.id,
                                periodoInicio = periodoInicioEpochDay,
                                periodoFin = periodoFinEpochDay,
                                totalCantidad = cantidad,
                                tarifaAplicada = empleado.tarifaBase,
                                montoTotal = monto,
                                estado = EstadoPago.PENDIENTE
                            )
                        )
                        generados++
                    }
                    generados
                }
            }
            _ui.update {
                val num = resultado.getOrDefault(0)
                it.copy(
                    calculando = false,
                    mensaje = resultado.exceptionOrNull()?.localizedMessage
                        ?: if (num > 0) "Se generaron $num pagos nuevos." else "No hay nuevos pagos para generar en este periodo."
                )
            }
        }
    }
}
