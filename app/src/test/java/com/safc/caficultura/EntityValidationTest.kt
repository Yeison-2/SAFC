package com.safc.caficultura

import com.safc.caficultura.data.local.RolUsuario
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.Produccion
import com.safc.caficultura.data.local.entity.TipoContrato
import com.safc.caficultura.data.local.entity.UnidadMedida
import com.safc.caficultura.data.local.entity.Usuario
import com.safc.caficultura.util.SafcValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityValidationTest {

    @Test
    fun `empleado con nombre vacio falla validacion`() {
        val empleado = Empleado(nombreCompleto = "", telefono = "123", tarifaBase = 2500.0)
        val resultado = SafcValidator.validarEmpleado(empleado)
        assertTrue(resultado.isFailure)
    }

    @Test
    fun `empleado con datos correctos pasa validacion`() {
        val empleado = Empleado(nombreCompleto = "Juan Perez", telefono = "123", tarifaBase = 2500.0)
        val resultado = SafcValidator.validarEmpleado(empleado)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `usuario con contrasena corta falla validacion`() {
        val usuario = Usuario(
            usuario = "admin",
            contrasena = "123",
            rol = RolUsuario.ADMIN
        )
        val resultado = SafcValidator.validarUsuario(usuario)
        assertTrue(resultado.isFailure)
    }

    @Test
    fun `usuario con datos correctos pasa validacion`() {
        val usuario = Usuario(
            usuario = "superadmin",
            contrasena = "super123",
            rol = RolUsuario.SUPER_ADMIN
        )
        val resultado = SafcValidator.validarUsuario(usuario)
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `produccion con valor cero falla validacion`() {
        val produccion = Produccion(
            empleadoId = 1,
            fecha = 123456,
            valor = 0.0,
            valorOriginal = 0.0,
            unidad = UnidadMedida.KG
        )
        val resultado = SafcValidator.validarProduccion(produccion)
        assertTrue(resultado.isFailure)
    }

    @Test
    fun `produccion con valor positivo pasa validacion`() {
        val produccion = Produccion(
            empleadoId = 1,
            fecha = 123456,
            valor = 10.0,
            valorOriginal = 10.0,
            unidad = UnidadMedida.KG
        )
        val resultado = SafcValidator.validarProduccion(produccion)
        assertTrue(resultado.isSuccess)
    }
}
