package com.example.protocampo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protocampo.repository.data.AppDatabase
import com.example.protocampo.repository.data.PreferenciasDataStore
import com.example.protocampo.repository.models.Tecnico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Errores por campo del formulario de login (null = sin error). */
data class LoginErrores(
    val email: String? = null,
    val password: String? = null
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val recordarEmail: Boolean = true,
    val verificandoSesion: Boolean = true, // loader mientras se lee DataStore
    val cargando: Boolean = false,
    val errores: LoginErrores = LoginErrores(),
    val errorGeneral: String? = null,
    val tecnico: Tecnico? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tecnicoDao = AppDatabase.getDatabase(application).tecnicoDao()
    private val preferencias = PreferenciasDataStore(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Al abrir la app: recupera el correo recordado y la sesión guardada (DataStore)
        viewModelScope.launch {
            val emailGuardado = preferencias.emailRecordado.first()
            val idSesion = preferencias.sesionTecnicoId.first()
            val tecnico = idSesion?.let { withContext(Dispatchers.IO) { tecnicoDao.getById(it) } }
            _uiState.update {
                it.copy(email = emailGuardado, tecnico = tecnico, verificandoSesion = false)
            }
        }
    }

    fun onEmailChange(valor: String) =
        _uiState.update { it.copy(email = valor, errores = it.errores.copy(email = null), errorGeneral = null) }

    fun onPasswordChange(valor: String) =
        _uiState.update { it.copy(password = valor, errores = it.errores.copy(password = null), errorGeneral = null) }

    fun onRecordarEmailChange(valor: Boolean) = _uiState.update { it.copy(recordarEmail = valor) }

    /** Valida todos los campos; devuelve true si el formulario es válido. */
    private fun validar(): Boolean {
        val estado = _uiState.value
        val errores = LoginErrores(
            email = Validaciones.email(estado.email),
            password = Validaciones.password(estado.password)
        )
        _uiState.update { it.copy(errores = errores) }
        return errores.email == null && errores.password == null
    }

    fun login() {
        if (!validar()) return
        val estado = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            delay(700) // pausa breve para que se vea el indicador de carga
            val tecnico = withContext(Dispatchers.IO) {
                tecnicoDao.login(estado.email.trim(), estado.password)
            }
            if (tecnico != null) {
                preferencias.guardarSesion(tecnico.id)
                preferencias.guardarEmailRecordado(if (estado.recordarEmail) estado.email.trim() else "")
                _uiState.update { it.copy(cargando = false, tecnico = tecnico, password = "") }
            } else {
                _uiState.update { it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos") }
            }
        }
    }

    fun logout() {
        // Se limpia el estado de inmediato (antes de volver al Login) y luego la sesión en DataStore
        val recordar = _uiState.value.recordarEmail
        _uiState.value = LoginUiState(
            email = if (recordar) _uiState.value.email else "",
            recordarEmail = recordar,
            verificandoSesion = false
        )
        viewModelScope.launch { preferencias.guardarSesion(null) }
    }
}
