package com.example.protocampo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protocampo.repository.data.PreferenciasDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Preferencias persistentes con DataStore.
 * Se usa Boolean? (null = todavía cargando) para poder mostrar un loader,
 * igual que en la Guía 12.
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = PreferenciasDataStore(application)

    val temaOscuro: StateFlow<Boolean?> = preferencias.temaOscuro
        .stateIn<Boolean?>(viewModelScope, SharingStarted.Eagerly, null)

    val gpsAutomatico: StateFlow<Boolean?> = preferencias.gpsAutomatico
        .stateIn<Boolean?>(viewModelScope, SharingStarted.Eagerly, null)

    fun cambiarTemaOscuro(valor: Boolean) {
        viewModelScope.launch { preferencias.guardarTemaOscuro(valor) }
    }

    fun cambiarGpsAutomatico(valor: Boolean) {
        viewModelScope.launch { preferencias.guardarGpsAutomatico(valor) }
    }
}
