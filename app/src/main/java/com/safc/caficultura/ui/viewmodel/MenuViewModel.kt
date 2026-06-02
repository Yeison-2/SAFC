package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.safc.caficultura.AppDependencies
import com.safc.caficultura.data.local.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MenuViewModel(private val deps: AppDependencies) : ViewModel() {

    private val hoy = LocalDate.now()
    private val hoyEpoch = hoy.toEpochDay()
    
    private val inicioMs = hoy.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val finMs = hoy.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sumaKgHoy: Flow<Double> = deps.sesion.flatMapLatest { sesion ->
        val empId = sesion?.usuario?.empleadoId
        if (sesion?.usuario?.rol == RolUsuario.ADMIN || sesion?.usuario?.rol == RolUsuario.SUPER_ADMIN) {
            deps.database.produccionDao().observarSumaKgDelDia(hoyEpoch)
        } else if (empId != null) {
            deps.database.produccionDao().observarSumaKgDelDiaPorEmpleado(hoyEpoch, empId)
        } else {
            flowOf(0.0)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sumaJornalesHoy: Flow<Double> = deps.sesion.flatMapLatest { sesion ->
        val empId = sesion?.usuario?.empleadoId
        if (empId != null && sesion.usuario.rol == RolUsuario.RECOLECTOR) {
            deps.database.produccionDao().observarSumaJornalesDelDiaPorEmpleado(hoyEpoch, empId)
        } else {
            flowOf(0.0)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sumaPagosHoy: Flow<Double> = deps.sesion.flatMapLatest { sesion ->
        val empId = sesion?.usuario?.empleadoId
        if (sesion?.usuario?.rol == RolUsuario.ADMIN || sesion?.usuario?.rol == RolUsuario.SUPER_ADMIN) {
            deps.database.pagoDao().observarSumaPagosRangoMs(inicioMs, finMs)
        } else if (empId != null) {
            deps.database.pagoDao().observarSumaPagosRangoMsPorEmpleado(inicioMs, finMs, empId)
        } else {
            flowOf(0.0)
        }
    }
}
