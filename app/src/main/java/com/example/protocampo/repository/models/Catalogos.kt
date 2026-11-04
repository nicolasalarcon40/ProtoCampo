package com.example.protocampo.repository.models

/** Listas fijas que usan los formularios (selectores). */
object Catalogos {

    val tiposEquipo = listOf(
        "Transformador de poder",
        "Relé de protección",
        "Celda de media tensión",
        "Cable de poder",
        "Interruptor"
    )

    /** Cada tipo de prueba tiene su unidad de medida. */
    data class TipoPrueba(val nombre: String, val unidad: String)

    val tiposPrueba = listOf(
        TipoPrueba("Resistencia de aislamiento", "MΩ"),
        TipoPrueba("Resistencia de devanados", "mΩ"),
        TipoPrueba("Relación de transformación (TTR)", "razón"),
        TipoPrueba("Tiempo de operación", "ms")
    )

    fun unidadDe(tipoPrueba: String): String =
        tiposPrueba.firstOrNull { it.nombre == tipoPrueba }?.unidad ?: ""

    val resultados = listOf("Aprobado", "Observado", "Rechazado")
}
