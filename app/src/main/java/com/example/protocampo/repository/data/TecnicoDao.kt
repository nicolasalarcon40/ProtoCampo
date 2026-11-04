package com.example.protocampo.repository.data

import androidx.room.Dao
import androidx.room.Query
import com.example.protocampo.repository.models.Tecnico

@Dao
interface TecnicoDao {
    @Query("SELECT * FROM tecnicos WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Tecnico?

    @Query("SELECT * FROM tecnicos WHERE id = :id")
    suspend fun getById(id: Int): Tecnico?
}
