package com.example.protocampo.view.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.protocampo.repository.models.Equipo
import com.example.protocampo.view.theme.AmarilloObservado
import com.example.protocampo.view.utils.ChipResultado
import com.example.protocampo.viewmodels.EquipoViewModel
import com.example.protocampo.viewmodels.FilaHistorial
import com.example.protocampo.viewmodels.ProtocoloTexto
import com.example.protocampo.viewmodels.ProtocoloViewModel
import kotlin.math.abs

/** Ficha del equipo + historial de pruebas comparado con la medición anterior. */
@Composable
fun EquipoDetalleScreen(
    equipoViewModel: EquipoViewModel,
    protocoloViewModel: ProtocoloViewModel,
    equipoId: Int,
    onEditar: (Int) -> Unit,
    onNuevoProtocolo: (Int) -> Unit,
    onVerProtocolo: (Int) -> Unit,
    onEliminado: () -> Unit
) {
    val equipo by remember(equipoId) { equipoViewModel.observarEquipo(equipoId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val historial by remember(equipoId) { protocoloViewModel.historialEquipo(equipoId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    val e = equipo
    if (e == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FichaEquipo(e, onEditar = { onEditar(e.id) }, onEliminar = { confirmarEliminar = true })
            }
            item {
                Text(
                    "Historial de pruebas (${historial.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (historial.isEmpty()) {
                    Text("Este equipo aún no tiene protocolos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(historial, key = { it.protocolo.id }) { fila ->
                FilaHistorialItem(fila) { onVerProtocolo(fila.protocolo.id) }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { onNuevoProtocolo(e.id) },
            icon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
            text = { Text("Nueva prueba") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    // Confirmación antes de borrar (AlertDialog)
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Eliminar ${e.codigo}?") },
            text = { Text("Se eliminarán también sus ${historial.size} protocolos. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    equipoViewModel.eliminar(e)
                    onEliminado()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FichaEquipo(e: Equipo, onEditar: () -> Unit, onEliminar: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (e.fotoUri != null) {
                AsyncImage(
                    model = e.fotoUri,
                    contentDescription = "Placa del equipo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            Text(e.codigo, style = MaterialTheme.typography.headlineMedium)
            Text(e.tipo, style = MaterialTheme.typography.titleMedium)
            Text("Marca: ${e.marca}")
            Text("Cliente: ${e.cliente}")
            Text("Instalación: ${e.instalacion}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text(" Editar")
                }
                OutlinedButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(" Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Una medición con su variación respecto de la anterior del mismo tipo de prueba. */
@Composable
private fun FilaHistorialItem(fila: FilaHistorial, onClick: () -> Unit) {
    val p = fila.protocolo
    var expandida by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // la tarjeta crece suavemente al expandirse
            .clickable { expandida = !expandida }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.tipoPrueba, style = MaterialTheme.typography.titleMedium)
                    Text(ProtocoloTexto.fecha(p.fechaHora), style = MaterialTheme.typography.bodySmall)
                }
                ChipResultado(p.resultado)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    "${ProtocoloTexto.numero(p.valorMedido)} ${p.unidad}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Variacion(fila.variacionPct)
            }
            if (expandida) {
                Text("Tensión: ${ProtocoloTexto.numero(p.tensionPruebaKv)} kV · Temp.: ${ProtocoloTexto.numero(p.temperaturaC)} °C")
                Text("Técnico: ${p.tecnicoNombre}")
                Text("Obs.: ${p.observaciones.ifBlank { "—" }}")
                TextButton(onClick = onClick) { Text("Ver protocolo completo") }
            }
        }
    }
}

@Composable
fun Variacion(pct: Double?) {
    if (pct == null) {
        Text("Primera medición", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val icono = when {
        pct > 1.0 -> Icons.Default.TrendingUp
        pct < -1.0 -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }
    // Una variación mayor a 10 % se destaca en ámbar para que el técnico la revise
    val color = if (abs(pct) > 10.0) AmarilloObservado else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(" ${ProtocoloTexto.variacion(pct)} vs anterior", color = color, style = MaterialTheme.typography.bodySmall)
    }
}
