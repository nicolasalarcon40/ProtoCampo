package com.example.protocampo.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.repository.models.Catalogos
import com.example.protocampo.view.utils.AnimacionEntradaFormulario
import com.example.protocampo.view.utils.CameraCapture
import com.example.protocampo.view.utils.CampoTexto
import com.example.protocampo.view.utils.PhotoBox
import com.example.protocampo.view.utils.SelectorLista
import com.example.protocampo.viewmodels.EquipoViewModel

@Composable
fun EquipoFormScreen(viewModel: EquipoViewModel, equipoId: Int, onListo: () -> Unit) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var mostrarCamara by rememberSaveable { mutableStateOf(false) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var iniciado by rememberSaveable { mutableStateOf(false) }

    // Carga el equipo solo la primera vez (no al rotar la pantalla)
    LaunchedEffect(equipoId) {
        if (!iniciado) {
            viewModel.iniciarFormulario(equipoId)
            iniciado = true
        }
        visible = true
    }

    // RECURSO NATIVO 1: cámara (CameraX) para la foto de la placa del equipo
    if (mostrarCamara) {
        CameraCapture(
            carpeta = "equipos",
            onImageCaptured = { uri ->
                viewModel.onFotoChange(uri.toString())
                mostrarCamara = false
            },
            onClose = { mostrarCamara = false }
        )
        return
    }

    // ANIMACIÓN 1: el formulario entra con fade + deslizamiento
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (equipoId > 0) "Editar equipo" else "Nuevo equipo",
                style = MaterialTheme.typography.headlineMedium
            )
            PhotoBox(imageUri = form.fotoUri, texto = "Foto de la placa", onTakePhoto = { mostrarCamara = true })

            CampoTexto(
                valor = form.codigo, onValorChange = viewModel::onCodigoChange,
                etiqueta = "Código del equipo", icono = Icons.Default.QrCode,
                error = form.errores.codigo, ayuda = "Ej: TR-001"
            )
            SelectorLista(
                etiqueta = "Tipo de equipo", opciones = Catalogos.tiposEquipo,
                seleccionado = form.tipo, onSeleccion = viewModel::onTipoChange,
                icono = Icons.Default.Category, error = form.errores.tipo
            )
            CampoTexto(
                valor = form.marca, onValorChange = viewModel::onMarcaChange,
                etiqueta = "Marca / fabricante", icono = Icons.Default.Sell, error = form.errores.marca
            )
            CampoTexto(
                valor = form.cliente, onValorChange = viewModel::onClienteChange,
                etiqueta = "Cliente", icono = Icons.Default.Business, error = form.errores.cliente
            )
            CampoTexto(
                valor = form.instalacion, onValorChange = viewModel::onInstalacionChange,
                etiqueta = "Instalación / ubicación", icono = Icons.Default.Factory,
                error = form.errores.instalacion, ayuda = "Ej: S/E Norte, Antofagasta"
            )

            Button(
                onClick = { viewModel.guardar(onExito = onListo) },
                enabled = !form.guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (form.guardando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Guardar equipo")
            }
            OutlinedButton(onClick = onListo, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
