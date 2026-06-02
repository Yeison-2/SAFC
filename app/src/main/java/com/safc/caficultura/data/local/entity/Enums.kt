package com.safc.caficultura.data.local.entity

enum class TipoContrato {
    PESO, // Por kg o arroba
    DIA   // Por jornal / día
}

enum class UnidadMedida {
    KG,
    ARROBA,
    JORNAL
}

enum class EstadoPago {
    PENDIENTE,
    PAGADO,
    CANCELADO
}
