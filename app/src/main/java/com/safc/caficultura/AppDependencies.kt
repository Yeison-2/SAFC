package com.safc.caficultura

import android.content.Context
import com.safc.caficultura.config.MailConfiguration
import com.safc.caficultura.data.local.SafcDatabase
import com.safc.caficultura.data.local.dao.UsuarioConDetalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppDependencies(context: Context) {

    val database: SafcDatabase = SafcDatabase.crear(context.applicationContext)
    val mailConfig = MailConfiguration()

    private val _sesion = MutableStateFlow<UsuarioConDetalle?>(null)
    val sesion: StateFlow<UsuarioConDetalle?> = _sesion.asStateFlow()

    fun establecerSesion(detalle: UsuarioConDetalle?) {
        _sesion.value = detalle
    }

    fun cerrarSesion() {
        _sesion.value = null
    }
}
