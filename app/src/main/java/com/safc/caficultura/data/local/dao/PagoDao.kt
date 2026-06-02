package com.safc.caficultura.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.EstadoPago
import com.safc.caficultura.data.local.entity.Pago
import kotlinx.coroutines.flow.Flow

data class PagoConEmpleado(
    @Embedded val pago: Pago,
    @Relation(
        parentColumn = "empleadoId",
        entityColumn = "id"
    )
    val empleado: Empleado
)

@Dao
interface PagoDao {

    @Transaction
    @Query("SELECT * FROM pagos ORDER BY fechaGeneracion DESC, empleadoId ASC")
    fun observarTodosConEmpleado(): Flow<List<PagoConEmpleado>>

    @Transaction
    @Query(
        """
        SELECT * FROM pagos
        WHERE periodoInicio >= :desdeEpochDay AND periodoFin <= :hastaEpochDay
        ORDER BY fechaGeneracion DESC
        """
    )
    fun observarPorRangoPeriodo(desdeEpochDay: Long, hastaEpochDay: Long): Flow<List<PagoConEmpleado>>

    @Transaction
    @Query("SELECT * FROM pagos WHERE empleadoId = :empleadoId ORDER BY fechaGeneracion DESC")
    fun observarPorEmpleado(empleadoId: Long): Flow<List<PagoConEmpleado>>

    @Query("SELECT COALESCE(SUM(montoTotal), 0) FROM pagos WHERE (fechaGeneracion >= :inicioMs AND fechaGeneracion <= :finMs) AND estado != 'CANCELADO'")
    fun observarSumaPagosRangoMs(inicioMs: Long, finMs: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(montoTotal), 0) FROM pagos WHERE (fechaGeneracion >= :inicioMs AND fechaGeneracion <= :finMs) AND empleadoId = :empleadoId AND estado != 'CANCELADO'")
    fun observarSumaPagosRangoMsPorEmpleado(inicioMs: Long, finMs: Long, empleadoId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pago: Pago): Long

    @Query("UPDATE pagos SET estado = :nuevoEstado WHERE id = :pagoId")
    suspend fun actualizarEstado(pagoId: Long, nuevoEstado: EstadoPago)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM pagos 
            WHERE empleadoId = :empleadoId AND periodoInicio = :inicio AND periodoFin = :fin
        )
        """
    )
    suspend fun existePagoEnPeriodo(empleadoId: Long, inicio: Long, fin: Long): Boolean
}
