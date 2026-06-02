package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.dao.PagoConEmpleado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf

data class ReportesFiltroEpoch(
    /** Epoch day inclusive; null = sin límite */
    val desde: Long? = null,
    /** Epoch day inclusive; null = sin límite */
    val hasta: Long? = null
)

class ReportesViewModel(
    private val deps: AppDependencies
) : ViewModel() {

    private val _filtro = MutableStateFlow(ReportesFiltroEpoch())
    val filtro: StateFlow<ReportesFiltroEpoch> = _filtro.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagosVisibles: StateFlow<List<PagoConEmpleado>> = combine(
        deps.sesion.flatMapLatest { sesion ->
            val rol = sesion?.usuario?.rol
            val empId = sesion?.usuario?.empleadoId
            when {
                rol == RolUsuario.ADMIN || rol == RolUsuario.SUPER_ADMIN -> 
                    deps.database.pagoDao().observarTodosConEmpleado()
                empId != null -> 
                    deps.database.pagoDao().observarPorEmpleado(empId)
                else -> flowOf(emptyList())
            }
        },
        _filtro
    ) { lista, filtro ->
        val desde = filtro.desde
        val hasta = filtro.hasta
        lista.filter { fila ->
            solapa(
                inicioPago = fila.pago.periodoInicio,
                finPago = fila.pago.periodoFin,
                desde = desde,
                hasta = hasta
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun establecerFiltro(desdeEpochDay: Long?, hastaEpochDay: Long?) {
        val normalizado = when {
            desdeEpochDay != null && hastaEpochDay != null && hastaEpochDay < desdeEpochDay ->
                ReportesFiltroEpoch(desde = desdeEpochDay, hasta = desdeEpochDay)

            else -> ReportesFiltroEpoch(desde = desdeEpochDay, hasta = hastaEpochDay)
        }
        _filtro.value = normalizado
    }

    companion object {
        private fun solapa(
            inicioPago: Long,
            finPago: Long,
            desde: Long?,
            hasta: Long?
        ): Boolean {
            if (desde == null && hasta == null) return true
            val limiteInferior = desde ?: Long.MIN_VALUE
            val limiteSuperior = hasta ?: Long.MAX_VALUE
            return inicioPago <= limiteSuperior && finPago >= limiteInferior
        }
    }
}
