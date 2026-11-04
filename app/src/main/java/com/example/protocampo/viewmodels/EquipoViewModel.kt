package com.example.protocampo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protocampo.repository.data.AppDatabase
import com.example.protocampo.repository.models.Equipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EquipoErrores(
    val codigo: String? = null,
    val tipo: String? = null,
    val marca: String? = null,
    val cliente: String? = null,
    val instalacion: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(codigo, tipo, marca, cliente, instalacion).any { it != null }
}

data class EquipoUiState(
    val id: Int = 0,
    val codigo: String = "",
    val tipo: String = "",
    val marca: String = "",
    val cliente: String = "",
    val instalacion: String = "",
    val fotoUri: String? = null,
    val errores: EquipoErrores = EquipoErrores(),
    val guardando: Boolean = false
)

class EquipoViewModel(application: Application) : AndroidViewModel(application) {

    private val equipoDao = AppDatabase.getDatabase(application).equipoDao()

    // READ: lista reactiva de equipos
    val equipos: StateFlow<List<Equipo>> = equipoDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado del formulario (crear / editar)
    private val _form = MutableStateFlow(EquipoUiState())
    val form: StateFlow<EquipoUiState> = _form.asStateFlow()

    fun observarEquipo(id: Int): Flow<Equipo?> = equipoDao.observarPorId(id)

    /** Prepara el formulario: vacío si id = 0, o con los datos del equipo a editar. */
    fun iniciarFormulario(id: Int) {
        if (id == 0) {
            _form.value = EquipoUiState()
            return
        }
        viewModelScope.launch {
            val equipo = withContext(Dispatchers.IO) { equipoDao.getById(id) } ?: return@launch
            _form.value = EquipoUiState(
                id = equipo.id, codigo = equipo.codigo, tipo = equipo.tipo, marca = equipo.marca,
                cliente = equipo.cliente, instalacion = equipo.instalacion, fotoUri = equipo.fotoUri
            )
        }
    }

    // --- Actualización por campo: guarda el valor y limpia el error de ese campo ---
    fun onCodigoChange(v: String) = _form.update {
        it.copy(codigo = v.uppercase(), errores = it.errores.copy(codigo = null))
    }
    fun onTipoChange(v: String) = _form.update { it.copy(tipo = v, errores = it.errores.copy(tipo = null)) }
    fun onMarcaChange(v: String) = _form.update { it.copy(marca = v, errores = it.errores.copy(marca = null)) }
    fun onClienteChange(v: String) = _form.update { it.copy(cliente = v, errores = it.errores.copy(cliente = null)) }
    fun onInstalacionChange(v: String) =
        _form.update { it.copy(instalacion = v, errores = it.errores.copy(instalacion = null)) }
    fun onFotoChange(uri: String?) = _form.update { it.copy(fotoUri = uri) }

    private fun validar(): Boolean {
        val f = _form.value
        val errores = EquipoErrores(
            codigo = Validaciones.codigoEquipo(f.codigo),
            tipo = Validaciones.seleccion(f.tipo, "el tipo de equipo"),
            marca = Validaciones.obligatorio(f.marca, "La marca", minimo = 2),
            cliente = Validaciones.obligatorio(f.cliente, "El cliente"),
            instalacion = Validaciones.obligatorio(f.instalacion, "La instalación")
        )
        _form.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    /** CREATE / UPDATE. Llama a onExito solo si se guardó correctamente. */
    fun guardar(onExito: () -> Unit) {
        if (!validar()) return
        val f = _form.value
        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            // Regla de negocio: el código del equipo no se puede repetir
            val repetidos = withContext(Dispatchers.IO) { equipoDao.contarPorCodigo(f.codigo.trim(), f.id) }
            if (repetidos > 0) {
                _form.update {
                    it.copy(guardando = false, errores = it.errores.copy(codigo = "Ya existe un equipo con ese código"))
                }
                return@launch
            }
            val equipo = Equipo(
                id = f.id, codigo = f.codigo.trim(), tipo = f.tipo, marca = f.marca.trim(),
                cliente = f.cliente.trim(), instalacion = f.instalacion.trim(), fotoUri = f.fotoUri
            )
            withContext(Dispatchers.IO) {
                if (f.id == 0) equipoDao.insert(equipo) else equipoDao.update(equipo)
            }
            _form.update { it.copy(guardando = false) }
            onExito()
        }
    }

    // DELETE
    fun eliminar(equipo: Equipo) {
        viewModelScope.launch(Dispatchers.IO) { equipoDao.delete(equipo) }
    }
}
