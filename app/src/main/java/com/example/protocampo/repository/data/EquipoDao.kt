package com.example.protocampo.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.protocampo.repository.models.Equipo
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipoDao {
    // READ (reactivo con Flow: la lista se actualiza sola)
    @Query("SELECT * FROM equipos ORDER BY codigo ASC")
    fun getAll(): Flow<List<Equipo>>

    @Query("SELECT * FROM equipos WHERE id = :id")
    suspend fun getById(id: Int): Equipo?

    @Query("SELECT * FROM equipos WHERE id = :id")
    fun observarPorId(id: Int): Flow<Equipo?>

    /** Para validar que el código sea único (excluye al propio equipo al editar). */
    @Query("SELECT COUNT(*) FROM equipos WHERE UPPER(codigo) = UPPER(:codigo) AND id != :excluirId")
    suspend fun contarPorCodigo(codigo: String, excluirId: Int): Int

    @Query("SELECT COUNT(*) FROM equipos")
    fun contar(): Flow<Int>

    // CREATE
    @Insert
    suspend fun insert(equipo: Equipo): Long

    // UPDATE
    @Update
    suspend fun update(equipo: Equipo)

    // DELETE (sus protocolos se borran por la FK CASCADE)
    @Delete
    suspend fun delete(equipo: Equipo)
}
