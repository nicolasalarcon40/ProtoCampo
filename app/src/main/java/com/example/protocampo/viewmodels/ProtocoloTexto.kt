package com.example.protocampo.viewmodels

import com.example.protocampo.repository.models.ProtocoloConEquipo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatos de texto usados por las pantallas (fecha, números y protocolo para compartir). */
object ProtocoloTexto {

    private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL"))

    fun fecha(millis: Long): String = formatoFecha.format(Date(millis))

    fun numero(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else String.format(Locale("es", "CL"), "%.2f", valor)

    fun variacion(pct: Double): String = String.format(Locale("es", "CL"), "%+.1f %%", pct)

    /** Texto del protocolo listo para enviar al cliente (correo, WhatsApp, etc.). */
    fun paraCompartir(item: ProtocoloConEquipo): String {
        val p = item.protocolo
        val ubicacion = if (p.latitud != null && p.longitud != null)
            "https://maps.google.com/?q=${p.latitud},${p.longitud}" else "No registrada"
        return """
            PROTOCOLO DE PRUEBA N° ${p.id} — Induztek Ingeniería SpA
            Fecha y hora: ${fecha(p.fechaHora)}
            Técnico: ${p.tecnicoNombre}

            EQUIPO
            Código: ${item.equipoCodigo} (${item.equipoTipo})
            Cliente: ${item.equipoCliente}
            Instalación: ${item.equipoInstalacion}
            Ubicación GPS: $ubicacion

            PRUEBA
            Tipo: ${p.tipoPrueba}
            Valor medido: ${numero(p.valorMedido)} ${p.unidad}
            Tensión de prueba: ${numero(p.tensionPruebaKv)} kV
            Temperatura: ${numero(p.temperaturaC)} °C
            Resultado: ${p.resultado.uppercase()}
            Observaciones: ${p.observaciones.ifBlank { "—" }}
        """.trimIndent()
    }
}
