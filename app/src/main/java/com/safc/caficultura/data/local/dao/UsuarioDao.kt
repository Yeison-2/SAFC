package com.safc.caficultura.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.Usuario

data class UsuarioConDetalle(
    @Embedded val usuario: Usuario,
    @Relation(
        parentColumn = "empleadoId",
        entityColumn = "id"
    )
    val empleado: Empleado?
)

@Dao
interface UsuarioDao {

    @Transaction
    @Query(
        """
        SELECT * FROM usuarios
        WHERE usuario = :nombreUsuario AND contrasena = :contrasena
        LIMIT 1
        """
    )
    suspend fun autenticar(nombreUsuario: String, contrasena: String): UsuarioConDetalle?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT * FROM usuarios WHERE empleadoId = :empleadoId LIMIT 1")
    suspend fun obtenerPorEmpleadoId(empleadoId: Long): Usuario?

    @Query("UPDATE usuarios SET usuario = :usuario, contrasena = :contrasena WHERE empleadoId = :empleadoId")
    suspend fun actualizarCredencialesPorEmpleado(empleadoId: Long, usuario: String, contrasena: String)
}
