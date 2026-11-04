package com.example.protocampo.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Equipo eléctrico probado en terreno (transformador, relé, celda, cable...).
 * El código del equipo es único: permite reunir su historial de pruebas.
 */
@Entity(tableName = "equipos", indices = [Index(value = ["codigo"], unique = true)])
data class Equipo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val tipo: String,
    val marca: String,
    val cliente: String,
    val instalacion: String,
    val fotoUri: String? = null // Foto de la placa del equipo (cámara)
)
