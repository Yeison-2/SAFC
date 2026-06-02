package com.safc.caficultura.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.Produccion
import kotlinx.coroutines.flow.Flow

data class ProduccionConEmpleado(
    @Embedded val produccion: Produccion,
    @Relation(
        parentColumn = "empleadoId",
        entityColumn = "id"
    )
    val empleado: Empleado
)

@Dao
interface ProduccionDao {

    @Transaction
    @Query("SELECT * FROM producciones WHERE empleadoId = :empleadoId ORDER BY timestamp DESC")
    fun observarPorEmpleado(empleadoId: Long): Flow<List<ProduccionConEmpleado>>

    @Transaction
    @Query("SELECT * FROM producciones ORDER BY timestamp DESC, empleadoId ASC")
    fun observarTodasConEmpleado(): Flow<List<ProduccionConEmpleado>>

    @Query(
        """
        SELECT COALESCE(SUM(valor), 0)
        FROM producciones
        WHERE empleadoId = :empleadoId AND fecha BETWEEN :desdeEpochDay AND :hastaEpochDay
        """
    )
    suspend fun sumarKgEnRango(empleadoId: Long, desdeEpochDay: Long, hastaEpochDay: Long): Double

    @Query(
        """
        SELECT COUNT(DISTINCT fecha)
        FROM producciones
        WHERE empleadoId = :empleadoId AND fecha BETWEEN :desdeEpochDay AND :hastaEpochDay
        """
    )
    suspend fun contarDiasEnRango(empleadoId: Long, desdeEpochDay: Long, hastaEpochDay: Long): Int

    @Query("SELECT COALESCE(SUM(valor), 0) FROM producciones WHERE fecha = :epochDay AND unidad != 'JORNAL'")
    fun observarSumaKgDelDia(epochDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(valor), 0) FROM producciones WHERE fecha = :epochDay AND empleadoId = :empleadoId AND unidad != 'JORNAL'")
    fun observarSumaKgDelDiaPorEmpleado(epochDay: Long, empleadoId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(valor), 0) FROM producciones WHERE fecha = :epochDay AND empleadoId = :empleadoId AND unidad = 'JORNAL'")
    fun observarSumaJornalesDelDiaPorEmpleado(epochDay: Long, empleadoId: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(produccion: Produccion): Long

    @Delete
    suspend fun eliminar(produccion: Produccion)
}
