package com.safc.caficultura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safc.caficultura.data.local.entity.Empleado
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpleadoDao {

    @Query("SELECT * FROM empleados WHERE activo = 1 ORDER BY nombreCompleto COLLATE NOCASE")
    fun observarActivos(): Flow<List<Empleado>>

    @Query("SELECT * FROM empleados ORDER BY nombreCompleto COLLATE NOCASE")
    fun observarTodos(): Flow<List<Empleado>>

    @Query("SELECT * FROM empleados WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Empleado?

    @Query("SELECT * FROM empleados WHERE activo = 1 ORDER BY nombreCompleto COLLATE NOCASE")
    suspend fun listarActivos(): List<Empleado>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(empleado: Empleado): Long

    @Update
    suspend fun actualizar(empleado: Empleado)

    @Query("UPDATE empleados SET activo = :activo WHERE id = :id")
    suspend fun establecerActivo(id: Long, activo: Boolean)
}
