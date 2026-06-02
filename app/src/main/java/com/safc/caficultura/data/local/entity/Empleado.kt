package com.safc.caficultura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleados")
data class Empleado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreCompleto: String,
    val telefono: String,
    val tipoContrato: TipoContrato = TipoContrato.PESO,
    val tarifaBase: Double = 0.0,
    val activo: Boolean = true
)
