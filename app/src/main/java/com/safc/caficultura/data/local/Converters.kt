package com.safc.caficultura.data.local

import androidx.room.TypeConverter

import com.safc.caficultura.data.local.entity.EstadoPago
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.data.local.entity.UnidadMedida

class Converters {

    @TypeConverter
    fun deRol(valor: String): RolUsuario = RolUsuario.valueOf(valor)

    @TypeConverter
    fun rolAString(rol: RolUsuario): String = rol.name

    @TypeConverter
    fun deUnidad(valor: String): UnidadMedida = UnidadMedida.valueOf(valor)

    @TypeConverter
    fun unidadAString(u: UnidadMedida): String = u.name

    @TypeConverter
    fun deTipoContrato(valor: String): TipoContrato = TipoContrato.valueOf(valor)

    @TypeConverter
    fun tipoContratoAString(t: TipoContrato): String = t.name

    @TypeConverter
    fun deEstadoPago(valor: String): EstadoPago = EstadoPago.valueOf(valor)

    @TypeConverter
    fun estadoPagoAString(e: EstadoPago): String = e.name
}
