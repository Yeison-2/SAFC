package com.safc.caficultura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = Empleado::class,
            parentColumns = ["id"],
            childColumns = ["empleadoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["empleadoId", "periodoInicio", "periodoFin"], unique = true)
    ]
)
data class Pago(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empleadoId: Long,
    val periodoInicio: Long,
    val periodoFin: Long,
    val totalCantidad: Double, // kg o días según contrato
    val tarifaAplicada: Double,
    val montoTotal: Double,
    val estado: EstadoPago = EstadoPago.PENDIENTE,
    val fechaGeneracion: Long = System.currentTimeMillis()
)
