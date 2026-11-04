package com.example.protocampo.repository.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Una sola instancia de DataStore para toda la app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_protocampo")

/**
 * DataStore Preferences (reemplazo moderno de SharedPreferences).
 * Guarda datos SIMPLES de configuración; los datos del negocio van en Room.
 */
class PreferenciasDataStore(private val context: Context) {

    private object Claves {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val EMAIL_RECORDADO = stringPreferencesKey("email_recordado")
        val SESION_TECNICO_ID = intPreferencesKey("sesion_tecnico_id")
        val GPS_AUTOMATICO = booleanPreferencesKey("gps_automatico")
    }

    // ---- Lectura (Flow: se emite un valor nuevo cada vez que cambia) ----
    val temaOscuro: Flow<Boolean> = context.dataStore.data.map { it[Claves.TEMA_OSCURO] ?: false }
    val emailRecordado: Flow<String> = context.dataStore.data.map { it[Claves.EMAIL_RECORDADO] ?: "" }
    val sesionTecnicoId: Flow<Int?> = context.dataStore.data.map { it[Claves.SESION_TECNICO_ID] }
    val gpsAutomatico: Flow<Boolean> = context.dataStore.data.map { it[Claves.GPS_AUTOMATICO] ?: true }

    // ---- Escritura (suspend: se llaman desde una corrutina) ----
    suspend fun guardarTemaOscuro(valor: Boolean) {
        context.dataStore.edit { it[Claves.TEMA_OSCURO] = valor }
    }

    suspend fun guardarEmailRecordado(email: String) {
        context.dataStore.edit { it[Claves.EMAIL_RECORDADO] = email }
    }

    suspend fun guardarSesion(tecnicoId: Int?) {
        context.dataStore.edit {
            if (tecnicoId == null) it.remove(Claves.SESION_TECNICO_ID)
            else it[Claves.SESION_TECNICO_ID] = tecnicoId
        }
    }

    suspend fun guardarGpsAutomatico(valor: Boolean) {
        context.dataStore.edit { it[Claves.GPS_AUTOMATICO] = valor }
    }
}
