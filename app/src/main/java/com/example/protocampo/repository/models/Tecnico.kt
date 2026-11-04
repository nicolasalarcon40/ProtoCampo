package com.example.protocampo.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Ingeniero o técnico de terreno que ejecuta las pruebas. El email es único. */
@Entity(tableName = "tecnicos", indices = [Index(value = ["email"], unique = true)])
data class Tecnico(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val cargo: String
)
