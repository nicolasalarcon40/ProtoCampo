package com.example.protocampo.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.protocampo.repository.models.Protocolo
import com.example.protocampo.repository.models.ProtocoloConEquipo
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocoloDao {

    @Query(
        """
        SELECT p.*, e.codigo AS equipoCodigo, e.tipo AS equipoTipo,
               e.cliente AS equipoCliente, e.instalacion AS equipoInstalacion
        FROM protocolos p INNER JOIN equipos e ON p.equipoId = e.id
        ORDER BY p.fechaHora DESC
        """
    )
    fun getAllConEquipo(): Flow<List<ProtocoloConEquipo>>

    @Query(
        """
        SELECT p.*, e.codigo AS equipoCodigo, e.tipo AS equipoTipo,
               e.cliente AS equipoCliente, e.instalacion AS equipoInstalacion
        FROM protocolos p INNER JOIN equipos e ON p.equipoId = e.id
        WHERE p.id = :id
        """
    )
    fun observarConEquipo(id: Int): Flow<ProtocoloConEquipo?>

    /** Historial de un equipo, del más reciente al más antiguo. */
    @Query("SELECT * FROM protocolos WHERE equipoId = :equipoId ORDER BY fechaHora DESC")
    fun getPorEquipo(equipoId: Int): Flow<List<Protocolo>>

    @Query("SELECT * FROM protocolos WHERE id = :id")
    suspend fun getById(id: Int): Protocolo?

    /** Última medición del mismo equipo y tipo de prueba (para comparar). */
    @Query(
        """
        SELECT * FROM protocolos
        WHERE equipoId = :equipoId AND tipoPrueba = :tipoPrueba AND id != :excluirId
              AND fechaHora < :antesDe
        ORDER BY fechaHora DESC LIMIT 1
        """
    )
    suspend fun getAnterior(equipoId: Int, tipoPrueba: String, excluirId: Int, antesDe: Long): Protocolo?

    @Query("SELECT COUNT(*) FROM protocolos")
    fun contar(): Flow<Int>

    @Query("SELECT COUNT(*) FROM protocolos WHERE resultado = :resultado")
    fun contarPorResultado(resultado: String): Flow<Int>

    @Insert
    suspend fun insert(protocolo: Protocolo): Long

    @Update
    suspend fun update(protocolo: Protocolo)

    @Delete
    suspend fun delete(protocolo: Protocolo)
}
