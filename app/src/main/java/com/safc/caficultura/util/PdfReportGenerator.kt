package com.safc.caficultura.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.safc.caficultura.R
import com.safc.caficultura.data.local.dao.PagoConEmpleado
import com.safc.caficultura.data.local.entity.TipoContrato
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object PdfReportGenerator {

    fun generarYCompartirReporte(
        context: Context,
        pagos: List<PagoConEmpleado>,
        totalKg: Double,
        totalJornales: Double,
        costoTotal: Double
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val formatMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val formatFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val fechaActual = LocalDateTime.now().format(formatFecha)

        // Dibujar Icono de la App
        try {
            val iconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_principal)
            val scaledBitmap = Bitmap.createScaledBitmap(iconBitmap, 60, 60, false)
            canvas.drawBitmap(scaledBitmap, 50f, 25f, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        canvas.drawText("SAFC - REPORTE DE PRODUCCIÓN", 320f, 60f, titlePaint)

        paint.textSize = 10f
        paint.color = Color.DKGRAY
        canvas.drawText("Generado el: $fechaActual", 440f, 85f, paint)

        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 14f
        canvas.drawText("RESUMEN GENERAL", 50f, 130f, paint)
        
        paint.isFakeBoldText = false
        paint.textSize = 12f
        canvas.drawText("Producción en Peso: ${String.format(Locale.getDefault(), "%.1f", totalKg)} kg", 50f, 150f, paint)
        canvas.drawText("Trabajo por Día: ${totalJornales.toInt()} jornadas", 50f, 170f, paint)
        canvas.drawText("Costos Laborales Totales: ${formatMoneda.format(costoTotal)}", 50f, 190f, paint)
        
        var y = 240f
        paint.isFakeBoldText = true
        canvas.drawText("EMPLEADO", 50f, y, paint)
        canvas.drawText("TIPO", 220f, y, paint)
        canvas.drawText("CANTIDAD", 350f, y, paint)
        canvas.drawText("MONTO", 480f, y, paint)
        
        y += 5f
        canvas.drawLine(50f, y, 545f, y, paint)
        y += 20f

        paint.isFakeBoldText = false
        for (item in pagos) {
            if (y > 780) break
            
            val nombre = if (item.empleado.nombreCompleto.length > 20) 
                item.empleado.nombreCompleto.substring(0, 18) + "..." 
            else item.empleado.nombreCompleto

            val tipo = if (item.empleado.tipoContrato == TipoContrato.PESO) "Peso" else "Jornal"
            val cantidad = if (item.empleado.tipoContrato == TipoContrato.PESO) 
                "${item.pago.totalCantidad.toInt()} kg" 
            else "${item.pago.totalCantidad.toInt()} días"

            canvas.drawText(nombre, 50f, y, paint)
            canvas.drawText(tipo, 220f, y, paint)
            canvas.drawText(cantidad, 350f, y, paint)
            canvas.drawText(formatMoneda.format(item.pago.montoTotal), 480f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Reporte_SAFC_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            compartirPdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun compartirPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Producción SAFC")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte via"))
    }
}
