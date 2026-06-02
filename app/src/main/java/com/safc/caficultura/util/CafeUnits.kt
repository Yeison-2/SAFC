package com.safc.caficultura.util

import com.safc.caficultura.data.local.entity.UnidadMedida
import java.util.Locale

object CafeUnits {

    /**
     * Convierte una entrada del usuario (ej: 20.3 para 20 arrobas y 3 libras) 
     * a un valor decimal real de arrobas.
     */
    fun parseArrobas(input: String): Double {
        val parts = input.replace(",", ".").split(".")
        if (parts.size == 1) return parts[0].toDoubleOrNull() ?: 0.0
        
        val arrobas = parts[0].toDoubleOrNull() ?: 0.0
        val libras = parts[1].toDoubleOrNull() ?: 0.0
        
        // 1 Arroba = 25 Libras
        return arrobas + (libras / 25.0)
    }

    /**
     * Convierte arrobas decimales a KG.
     * En Colombia, 1 @ = 12.5 kg.
     */
    fun arrobasAKg(arrobas: Double): Double {
        return arrobas * 12.5
    }

    fun kgAArrobas(kg: Double): Double {
        return kg / 12.5
    }

    /**
     * Formatea arrobas para mostrar (ej: 20.3 @)
     */
    fun formatearArrobas(arrobasDecimales: Double): String {
        val arrobas = arrobasDecimales.toInt()
        val libras = ((arrobasDecimales - arrobas) * 25.0).toInt()
        return if (libras > 0) "$arrobas.$libras @" else "$arrobas @"
    }
}
