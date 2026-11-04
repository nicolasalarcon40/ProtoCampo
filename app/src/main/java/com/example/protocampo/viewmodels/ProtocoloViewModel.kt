package com.example.protocampo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protocampo.repository.data.AppDatabase
import com.example.protocampo.repository.models.Catalogos
import com.example.protocampo.repository.models.Equipo
import com.example.protocampo.repository.models.Protocolo
import com.example.protocampo.repository.models.ProtocoloConEquipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProtocoloErrores(
    val equipo: String? = null,
    val tipoPrueba: String? = null,
    val valorMedido: String? = null,
    val tension: String? = null,
    val temperatura: String? = null,
    val resultado: String? = null,
    val observaciones: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(equipo, tipoPrueba, valorMedido, tension, temperatura, resultado, observaciones)
            .any { it != null }
}

data class ProtocoloUiState(
    val id: Int = 0,
    val equipoId: Int? = null,
    val tipoPrueba: String = "",
    val valorMedido: String = "",
    val tension: String = "",
    val temperatura: String = "",
    val resultado: String = "",
    val observaciones: String = "",
    val fechaHora: Long = 0L,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val anterior: Protocolo? = null, // última medición del mismo equipo y prueba
    val errores: ProtocoloErrores = ProtocoloErrores(),
    val guardando: Boolean = false,
    val mensajeExito: Boolean = false
) {
    /** Unidad según el tipo de prueba elegido (valor derivado del estado). */
    val unidad: String get() = Catalogos.unidadDe(tipoPrueba)
}

/** Resumen para la pantalla de inicio. */
data class ResumenUi(
    val totalProtocolos: Int = 0,
    val rechazados: Int = 0,
    val totalEquipos: Int = 0
)

/** Fila del historial de un equipo con su variación respecto a la medición anterior. */
data class FilaHistorial(val protocolo: Protocolo, val variacionPct: Double?)

class ProtocoloViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val protocoloDao = db.protocoloDao()
    private val equipoDao = db.equipoDao()

    // READ: lista de protocolos con datos del equipo (JOIN)
    val protocolos: StateFlow<List<ProtocoloConEquipo>> = protocoloDao.getAllConEquipo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equipos para el selector del formulario
    val equipos: StateFlow<List<Equipo>> = equipoDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contadores para el inicio (se combinan 3 flujos en uno)
    val resumen: StateFlow<ResumenUi> = combine(
        protocoloDao.contar(),
        protocoloDao.contarPorResultado("Rechazado"),
        equipoDao.contar()
    ) { total, rechazados, equipos -> ResumenUi(total, rechazados, equipos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenUi())

    private val _form = MutableStateFlow(ProtocoloUiState())
    val form: StateFlow<ProtocoloUiState> = _form.asStateFlow()

    fun observarDetalle(id: Int): Flow<ProtocoloConEquipo?> = protocoloDao.observarConEquipo(id)

    /** Historial de un equipo + variación % contra la medición anterior del mismo tipo de prueba. */
    fun historialEquipo(equipoId: Int): Flow<List<FilaHistorial>> =
        protocoloDao.getPorEquipo(equipoId).map { calcularVariaciones(it) }

    suspend fun buscarAnterior(p: Protocolo): Protocolo? = withContext(Dispatchers.IO) {
        protocoloDao.getAnterior(p.equipoId, p.tipoPrueba, p.id, p.fechaHora)
    }

    // ------------------------- Formulario -------------------------

    /** id = 0 → nuevo protocolo (opcionalmente con un equipo ya elegido). */
    fun iniciarFormulario(id: Int, equipoIdInicial: Int = 0) {
        if (id == 0) {
            _form.value = ProtocoloUiState(equipoId = equipoIdInicial.takeIf { it > 0 })
            cargarAnterior()
            return
        }
        viewModelScope.launch {
            val p = withContext(Dispatchers.IO) { protocoloDao.getById(id) } ?: return@launch
            _form.value = ProtocoloUiState(
                id = p.id, equipoId = p.equipoId, tipoPrueba = p.tipoPrueba,
                valorMedido = p.valorMedido.toString(), tension = p.tensionPruebaKv.toString(),
                temperatura = p.temperaturaC.toString(), resultado = p.resultado,
                observaciones = p.observaciones, fechaHora = p.fechaHora,
                latitud = p.latitud, longitud = p.longitud, fotoUri = p.fotoUri
            )
            cargarAnterior()
        }
    }

    fun onEquipoChange(id: Int) {
        _form.update { it.copy(equipoId = id, errores = it.errores.copy(equipo = null)) }
        cargarAnterior()
    }

    fun onTipoPruebaChange(v: String) {
        _form.update { it.copy(tipoPrueba = v, errores = it.errores.copy(tipoPrueba = null)) }
        cargarAnterior()
    }

    fun onValorChange(v: String) =
        _form.update { it.copy(valorMedido = v, errores = it.errores.copy(valorMedido = null)) }
    fun onTensionChange(v: String) =
        _form.update { it.copy(tension = v, errores = it.errores.copy(tension = null)) }
    fun onTemperaturaChange(v: String) =
        _form.update { it.copy(temperatura = v, errores = it.errores.copy(temperatura = null)) }
    fun onResultadoChange(v: String) =
        _form.update { it.copy(resultado = v, errores = it.errores.copy(resultado = null, observaciones = null)) }
    fun onObservacionesChange(v: String) =
        _form.update { it.copy(observaciones = v, errores = it.errores.copy(observaciones = null)) }
    fun onUbicacionChange(lat: Double, lon: Double) = _form.update { it.copy(latitud = lat, longitud = lon) }
    fun onFotoChange(uri: String?) = _form.update { it.copy(fotoUri = uri) }

    /** Busca la última medición del mismo equipo y tipo de prueba para mostrarla como referencia. */
    private fun cargarAnterior() {
        val f = _form.value
        val equipoId = f.equipoId
        if (equipoId == null || f.tipoPrueba.isBlank()) {
            _form.update { it.copy(anterior = null) }
            return
        }
        viewModelScope.launch {
            val antesDe = if (f.fechaHora > 0) f.fechaHora else System.currentTimeMillis()
            val anterior = withContext(Dispatchers.IO) {
                protocoloDao.getAnterior(equipoId, f.tipoPrueba, f.id, antesDe)
            }
            _form.update { it.copy(anterior = anterior) }
        }
    }

    private fun validar(): Boolean {
        val f = _form.value
        val errores = ProtocoloErrores(
            equipo = if (f.equipoId == null) "Selecciona el equipo probado" else null,
            tipoPrueba = Validaciones.seleccion(f.tipoPrueba, "el tipo de prueba"),
            valorMedido = Validaciones.numeroEnRango(f.valorMedido, "El valor medido", 0.001, 1_000_000.0),
            tension = Validaciones.numeroEnRango(f.tension, "La tensión de prueba", 0.0, 100.0),
            temperatura = Validaciones.numeroEnRango(f.temperatura, "La temperatura", -20.0, 60.0),
            resultado = Validaciones.seleccion(f.resultado, "el resultado"),
            observaciones = Validaciones.observaciones(f.observaciones, f.resultado)
        )
        _form.update { it.copy(errores = errores) }
        return !errores.hayErrores
    }

    /** CREATE / UPDATE. */
    fun guardar(tecnicoNombre: String, onExito: () -> Unit) {
        if (!validar()) return
        val f = _form.value
        viewModelScope.launch {
            _form.update { it.copy(guardando = true) }
            val protocolo = Protocolo(
                id = f.id,
                equipoId = f.equipoId!!,
                tipoPrueba = f.tipoPrueba,
                valorMedido = Validaciones.aNumero(f.valorMedido)!!,
                unidad = f.unidad,
                tensionPruebaKv = Validaciones.aNumero(f.tension)!!,
                temperaturaC = Validaciones.aNumero(f.temperatura)!!,
                resultado = f.resultado,
                observaciones = f.observaciones.trim(),
                fechaHora = if (f.fechaHora > 0) f.fechaHora else System.currentTimeMillis(),
                latitud = f.latitud,
                longitud = f.longitud,
                fotoUri = f.fotoUri,
                tecnicoNombre = tecnicoNombre
            )
            withContext(Dispatchers.IO) {
                if (f.id == 0) protocoloDao.insert(protocolo) else protocoloDao.update(protocolo)
            }
            // Muestra el mensaje animado "¡Protocolo guardado!" antes de volver
            _form.update { it.copy(guardando = false, mensajeExito = true) }
            delay(1200)
            onExito()
        }
    }

    // DELETE
    fun eliminar(protocolo: Protocolo) {
        viewModelScope.launch(Dispatchers.IO) { protocoloDao.delete(protocolo) }
    }

    companion object {
        /**
         * Lista ordenada de más reciente a más antigua. Para cada medición busca la
         * siguiente más antigua del MISMO tipo de prueba y calcula la variación en %.
         */
        fun calcularVariaciones(lista: List<Protocolo>): List<FilaHistorial> =
            lista.mapIndexed { i, actual ->
                val anterior = lista.drop(i + 1).firstOrNull { it.tipoPrueba == actual.tipoPrueba }
                FilaHistorial(actual, variacion(actual.valorMedido, anterior?.valorMedido))
            }

        fun variacion(actual: Double, anterior: Double?): Double? =
            if (anterior == null || anterior == 0.0) null else (actual - anterior) / anterior * 100.0
    }
}
