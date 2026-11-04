package com.example.protocampo.repository.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Protocolo de prueba registrado en terreno.
 * FK a equipos: si se elimina un equipo, se eliminan sus protocolos (CASCADE).
 */
@Entity(
    tableName = "protocolos",
    foreignKeys = [
        ForeignKey(
            entity = Equipo::class,
            parentColumns = ["id"],
            childColumns = ["equipoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("equipoId")]
)
data class Protocolo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipoId: Int,
    val tipoPrueba: String,
    val valorMedido: Double,
    val unidad: String,
    val tensionPruebaKv: Double,
    val temperaturaC: Double,
    val resultado: String,          // Aprobado / Observado / Rechazado
    val observaciones: String,
    val fechaHora: Long,            // milisegundos (System.currentTimeMillis)
    val latitud: Double? = null,    // GPS
    val longitud: Double? = null,   // GPS
    val fotoUri: String? = null,    // Evidencia fotográfica (cámara)
    val tecnicoNombre: String
)

/** Resultado de un JOIN protocolos + equipos (para listas y detalle). */
data class ProtocoloConEquipo(
    @Embedded val protocolo: Protocolo,
    val equipoCodigo: String,
    val equipoTipo: String,
    val equipoCliente: String,
    val equipoInstalacion: String
)
