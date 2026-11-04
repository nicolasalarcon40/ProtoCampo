package com.example.protocampo.view.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.repository.models.ProtocoloConEquipo
import com.example.protocampo.view.theme.RojoRechazado
import com.example.protocampo.view.utils.ChipResultado
import com.example.protocampo.viewmodels.ProtocoloTexto
import com.example.protocampo.viewmodels.ProtocoloViewModel

/** Pantalla de inicio (diseño Compact: celular en vertical, todo en una columna). */
@Composable
fun HomeScreen(
    viewModel: ProtocoloViewModel,
    nombreTecnico: String,
    onNuevoProtocolo: () -> Unit,
    onVerEquipos: () -> Unit,
    onVerProtocolo: (Int) -> Unit
) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val protocolos by viewModel.protocolos.collectAsStateWithLifecycle()
    val recientes = protocolos.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Hola, $nombreTecnico", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Registra tus pruebas en terreno y entrega el protocolo al instante.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TarjetaContador("Protocolos", resumen.totalProtocolos, Icons.Default.Assignment,
                MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            TarjetaContador("Equipos", resumen.totalEquipos, Icons.Default.ElectricalServices,
                MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            TarjetaContador("Rechazados", resumen.rechazados, Icons.Default.ReportProblem,
                RojoRechazado, Modifier.weight(1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNuevoProtocolo, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Default.NoteAdd, contentDescription = null)
                Text("  Nuevo protocolo de prueba")
            }
            OutlinedButton(onClick = onVerEquipos, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ElectricalServices, contentDescription = null)
                Text("  Ver equipos e historial")
            }
        }

        Text("Últimos protocolos", style = MaterialTheme.typography.titleLarge)
        if (recientes.isEmpty()) {
            Text("Aún no hay protocolos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        recientes.forEach { item -> FilaProtocolo(item) { onVerProtocolo(item.protocolo.id) } }
    }
}

/** Contador animado: el número "sube" hasta su valor (animateIntAsState). */
@Composable
private fun TarjetaContador(titulo: String, valor: Int, icono: ImageVector, color: Color, modifier: Modifier) {
    val valorAnimado by animateIntAsState(valor, animationSpec = tween(800), label = "contador")
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = color)
            Text("$valorAnimado", style = MaterialTheme.typography.headlineMedium, color = color)
            Text(titulo, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

/** Tarjeta de un protocolo (se reutiliza en Inicio y en la lista de protocolos). */
@Composable
fun FilaProtocolo(item: ProtocoloConEquipo, onClick: () -> Unit) {
    val p = item.protocolo
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("${item.equipoCodigo} · ${p.tipoPrueba}", style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${ProtocoloTexto.numero(p.valorMedido)} ${p.unidad} · ${ProtocoloTexto.fecha(p.fechaHora)}",
                    style = MaterialTheme.typography.bodySmall)
                Text(item.equipoCliente, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ChipResultado(p.resultado)
        }
    }
}
