package com.safc.caficultura.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
object Fechas {

    private val formatoIso = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseIso(texto: String): Result<LocalDate> =
        runCatching { LocalDate.parse(texto.trim(), formatoIso) }

    fun aEpochDay(localDate: LocalDate): Long = localDate.toEpochDay()

    fun desdeEpochDay(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun formatear(epochDay: Long): String = desdeEpochDay(epochDay).format(formatoIso)

    fun mensajeErrorParseo(): String =
        "Use fecha en formato yyyy-MM-dd (ejemplo: 2026-05-15)"
}
