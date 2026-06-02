package com.safc.caficultura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "producciones",
    foreignKeys = [
        ForeignKey(
            entity = Empleado::class,
            parentColumns = ["id"],
            childColumns = ["empleadoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Produccion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empleadoId: Long,
    val fecha: Long, // Epoch day
    val valor: Double, // Siempre convertido a KG para cálculos
    val valorOriginal: Double, // Lo que el usuario escribió (ej: 20.3)
    val unidad: UnidadMedida = UnidadMedida.KG,
    val timestamp: Long = System.currentTimeMillis()
)
