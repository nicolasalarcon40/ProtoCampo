package com.example.protocampo.view.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.repository.models.Catalogos
import com.example.protocampo.view.utils.AnimacionEntradaFormulario
import com.example.protocampo.view.utils.AparecerSuave
import com.example.protocampo.view.utils.CameraCapture
import com.example.protocampo.view.utils.CampoTexto
import com.example.protocampo.view.utils.MensajeExito
import com.example.protocampo.view.utils.PhotoBox
import com.example.protocampo.view.utils.SelectorLista
import com.example.protocampo.view.utils.TarjetaUbicacion
import com.example.protocampo.view.utils.colorResultado
import com.example.protocampo.viewmodels.AjustesViewModel
import com.example.protocampo.viewmodels.ProtocoloTexto
import com.example.protocampo.viewmodels.ProtocoloViewModel

@Composable
fun ProtocoloFormScreen(
    viewModel: ProtocoloViewModel,
    ajustesViewModel: AjustesViewModel,
    protocoloId: Int,
    equipoIdInicial: Int,
    tecnicoNombre: String,
    onListo: () -> Unit
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val equipos by viewModel.equipos.collectAsStateWithLifecycle()
    val gpsAutomatico by ajustesViewModel.gpsAutomatico.collectAsStateWithLifecycle()
    var mostrarCamara by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var iniciado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(protocoloId) {
        if (!iniciado) {
            viewModel.iniciarFormulario(protocoloId, equipoIdInicial)
            iniciado = true
        }
        visible = true
    }

    // RECURSO NATIVO 1: cámara para la evidencia fotográfica de la prueba
    if (mostrarCamara) {
        CameraCapture(
            carpeta = "protocolos",
            onImageCaptured = { uri ->
                viewModel.onFotoChange(uri.toString())
                mostrarCamara = false
            },
            onClose = { mostrarCamara = false }
        )
        return
    }

    val equipoSeleccionado = equipos.firstOrNull { it.id == form.equipoId }

    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                if (protocoloId > 0) "Editar protocolo" else "Nuevo protocolo de prueba",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Técnico: $tecnicoNombre",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // --- Equipo y prueba ---
            SelectorLista(
                etiqueta = "Equipo probado",
                opciones = equipos.map { "${it.codigo} — ${it.tipo}" },
                seleccionado = equipoSeleccionado?.let { "${it.codigo} — ${it.tipo}" } ?: "",
                onSeleccion = { texto ->
                    equipos.firstOrNull { "${it.codigo} — ${it.tipo}" == texto }
                        ?.let { viewModel.onEquipoChange(it.id) }
                },
                icono = Icons.Default.ElectricalServices,
                error = form.errores.equipo
            )
            if (equipos.isEmpty()) {
                Text("Primero registra un equipo en la sección Equipos.", color = MaterialTheme.colorScheme.error)
            }
            SelectorLista(
                etiqueta = "Tipo de prueba",
                opciones = Catalogos.tiposPrueba.map { it.nombre },
                seleccionado = form.tipoPrueba,
                onSeleccion = viewModel::onTipoPruebaChange,
                icono = Icons.Default.Science,
                error = form.errores.tipoPrueba
            )

            // Medición anterior del mismo equipo (aparece con animación)
            AparecerSuave(visible = form.anterior != null) {
                form.anterior?.let { ant ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Column(Modifier.padding(start = 8.dp)) {
                                Text("Medición anterior", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${ProtocoloTexto.numero(ant.valorMedido)} ${ant.unidad} · " +
                                        "${ProtocoloTexto.fecha(ant.fechaHora)} · ${ant.resultado}"
                                )
                            }
                        }
                    }
                }
            }

            // --- Valores de medición ---
            CampoTexto(
                valor = form.valorMedido, onValorChange = viewModel::onValorChange,
                etiqueta = "Valor medido", icono = Icons.Default.Speed,
                error = form.errores.valorMedido, tipoTeclado = KeyboardType.Decimal,
                sufijo = form.unidad.ifBlank { null }
            )
            CampoTexto(
                valor = form.tension, onValorChange = viewModel::onTensionChange,
                etiqueta = "Tensión de prueba", icono = Icons.Default.Bolt,
                error = form.errores.tension, tipoTeclado = KeyboardType.Decimal,
                sufijo = "kV", ayuda = "Usa 0 si la prueba no aplica tensión"
            )
            CampoTexto(
                valor = form.temperatura, onValorChange = viewModel::onTemperaturaChange,
                etiqueta = "Temperatura ambiente", icono = Icons.Default.Thermostat,
                error = form.errores.temperatura, tipoTeclado = KeyboardType.Decimal, sufijo = "°C"
            )

            // --- Resultado (RadioButton con color animado) ---
            Text("Resultado de la prueba", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Catalogos.resultados.forEach { opcion ->
                    val color by animateColorAsState(
                        if (form.resultado == opcion) colorResultado(opcion)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "radio_$opcion"
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = form.resultado == opcion,
                            onClick = { viewModel.onResultadoChange(opcion) }
                        )
                    ) {
                        RadioButton(
                            selected = form.resultado == opcion,
                            onClick = { viewModel.onResultadoChange(opcion) },
                            colors = RadioButtonDefaults.colors(selectedColor = color, unselectedColor = color)
                        )
                        Text(opcion, color = color)
                    }
                }
            }
            AparecerSuave(visible = form.errores.resultado != null) {
                Text(form.errores.resultado ?: "", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            CampoTexto(
                valor = form.observaciones, onValorChange = viewModel::onObservacionesChange,
                etiqueta = "Observaciones", icono = Icons.Default.Notes,
                error = form.errores.observaciones, unaLinea = false,
                ayuda = if (form.resultado.isNotBlank() && form.resultado != "Aprobado")
                    "Obligatorio: explica el motivo" else "Opcional"
            )

            // RECURSO NATIVO 2: GPS
            TarjetaUbicacion(
                latitud = form.latitud,
                longitud = form.longitud,
                automatico = gpsAutomatico == true && protocoloId == 0,
                onUbicacion = viewModel::onUbicacionChange
            )

            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                PhotoBox(imageUri = form.fotoUri, texto = "Foto de evidencia\n(opcional)",
                    onTakePhoto = { mostrarCamara = true })
            }

            MensajeExito(visible = form.mensajeExito, texto = "¡Protocolo guardado!")

            val colorBoton by animateColorAsState(
                if (form.mensajeExito) colorResultado("Aprobado") else MaterialTheme.colorScheme.primary,
                label = "colorBoton"
            )
            Button(
                onClick = { if (!form.mensajeExito) viewModel.guardar(tecnicoNombre, onExito = onListo) },
                enabled = !form.guardando,
                colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (form.guardando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text(if (form.mensajeExito) "Guardado ✓" else "Guardar protocolo")
            }
            OutlinedButton(onClick = onListo, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
