package com.example.protocampo.view.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.protocampo.repository.models.Protocolo
import com.example.protocampo.view.utils.ChipResultado
import com.example.protocampo.viewmodels.ProtocoloTexto
import com.example.protocampo.viewmodels.ProtocoloViewModel
import java.util.Locale

@Composable
fun ProtocoloDetalleScreen(
    viewModel: ProtocoloViewModel,
    protocoloId: Int,
    onEditar: (Int) -> Unit,
    onEliminado: () -> Unit
) {
    val context = LocalContext.current
    val item by remember(protocoloId) { viewModel.observarDetalle(protocoloId) }
        .collectAsStateWithLifecycle(initialValue = null)
    var anterior by remember { mutableStateOf<Protocolo?>(null) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    // Busca la medición anterior del mismo equipo y prueba (corrutina)
    LaunchedEffect(item?.protocolo) {
        item?.protocolo?.let { anterior = viewModel.buscarAnterior(it) }
    }

    val datos = item
    if (datos == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val p = datos.protocolo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Protocolo N° ${p.id}", style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f))
                    ChipResultado(p.resultado)
                }
                Text(ProtocoloTexto.fecha(p.fechaHora), style = MaterialTheme.typography.bodyMedium)
                Text("Técnico: ${p.tecnicoNombre}", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Equipo", style = MaterialTheme.typography.titleMedium)
                Text("${datos.equipoCodigo} · ${datos.equipoTipo}")
                Text("Cliente: ${datos.equipoCliente}")
                Text("Instalación: ${datos.equipoInstalacion}")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(p.tipoPrueba, style = MaterialTheme.typography.titleMedium)
                Text("${ProtocoloTexto.numero(p.valorMedido)} ${p.unidad}",
                    style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                // Comparación con la medición anterior (mejora pedida en el caso)
                Variacion(ProtocoloViewModel.variacion(p.valorMedido, anterior?.valorMedido))
                anterior?.let {
                    Text(
                        "Anterior: ${ProtocoloTexto.numero(it.valorMedido)} ${it.unidad} (${ProtocoloTexto.fecha(it.fechaHora)})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text("Tensión de prueba: ${ProtocoloTexto.numero(p.tensionPruebaKv)} kV")
                Text("Temperatura: ${ProtocoloTexto.numero(p.temperaturaC)} °C")
                Text("Observaciones: ${p.observaciones.ifBlank { "—" }}")
                if (p.latitud != null && p.longitud != null) {
                    Text(String.format(Locale.US, "GPS: %.5f, %.5f", p.latitud, p.longitud),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (p.fotoUri != null) {
            AsyncImage(
                model = p.fotoUri,
                contentDescription = "Evidencia fotográfica",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
            )
        }

        // Entrega inmediata al cliente: comparte el protocolo por correo, WhatsApp, etc.
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Protocolo de prueba ${datos.equipoCodigo} — N° ${p.id}")
                    putExtra(Intent.EXTRA_TEXT, ProtocoloTexto.paraCompartir(datos))
                }
                context.startActivity(Intent.createChooser(intent, "Enviar protocolo al cliente"))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Text("  Enviar protocolo al cliente")
        }

        if (p.latitud != null && p.longitud != null) {
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("geo:${p.latitud},${p.longitud}?q=${p.latitud},${p.longitud}")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
                Text("  Ver ubicación en el mapa")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onEditar(p.id) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(" Editar")
            }
            OutlinedButton(onClick = { confirmarEliminar = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(" Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar protocolo N° ${p.id}?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar(p)
                    onEliminado()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}
