package com.safc.caficultura.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.safc.caficultura.data.local.RolUsuario

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = Empleado::class,
            parentColumns = ["id"],
            childColumns = ["empleadoId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["usuario"], unique = true),
        Index(value = ["empleadoId"])
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val contrasena: String,
    val rol: RolUsuario,
    val empleadoId: Long? = null
)
