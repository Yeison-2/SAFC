package com.safc.caficultura.util

import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.Usuario
import com.safc.caficultura.data.local.entity.Produccion

object SafcValidator {

    fun validarEmpleado(empleado: Empleado): Result<Unit> {
        if (empleado.nombreCompleto.isBlank()) {
            return Result.failure(Exception("El nombre completo no puede estar vacío"))
        }
        if (empleado.tarifaBase < 0) {
            return Result.failure(Exception("La tarifa base no puede ser negativa"))
        }
        return Result.success(Unit)
    }

    fun validarUsuario(usuario: Usuario): Result<Unit> {
        if (usuario.usuario.length < 4) {
            return Result.failure(Exception("El nombre de usuario debe tener al menos 4 caracteres"))
        }
        if (usuario.contrasena.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        return Result.success(Unit)
    }

    fun validarProduccion(produccion: Produccion): Result<Unit> {
        if (produccion.valorOriginal <= 0) {
            return Result.failure(Exception("La cantidad de producción debe ser mayor a cero"))
        }
        if (produccion.empleadoId <= 0) {
            return Result.failure(Exception("ID de empleado no válido"))
        }
        return Result.success(Unit)
    }
}
