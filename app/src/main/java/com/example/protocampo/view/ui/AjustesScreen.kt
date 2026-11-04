package com.example.protocampo.view.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.repository.models.Tecnico
import com.example.protocampo.viewmodels.AjustesViewModel

/** Preferencias guardadas con DataStore (sobreviven a cerrar la app). */
@Composable
fun AjustesScreen(viewModel: AjustesViewModel, tecnico: Tecnico?, onCerrarSesion: () -> Unit) {
    val temaOscuro by viewModel.temaOscuro.collectAsStateWithLifecycle()
    val gpsAutomatico by viewModel.gpsAutomatico.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(tecnico?.nombre ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(tecnico?.cargo ?: "", style = MaterialTheme.typography.bodySmall)
                    Text(tecnico?.email ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Boolean? → null mientras DataStore carga: se muestra un loader (Guía 12)
        if (temaOscuro == null || gpsAutomatico == null) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else {
            val oscuro = temaOscuro == true
            // derivedStateOf: texto que depende del estado
            val textoTema by remember(oscuro) {
                derivedStateOf { if (oscuro) "Modo oscuro activado" else "Modo claro activado" }
            }
            FilaPreferencia(
                icono = if (oscuro) Icons.Default.DarkMode else Icons.Default.LightMode,
                titulo = "Tema oscuro",
                descripcion = "$textoTema. Útil en salas eléctricas con poca luz.",
                activo = oscuro,
                onCambio = viewModel::cambiarTemaOscuro
            )
            FilaPreferencia(
                icono = Icons.Default.MyLocation,
                titulo = "GPS automático",
                descripcion = "Captura la ubicación al crear un protocolo",
                activo = gpsAutomatico == true,
                onCambio = viewModel::cambiarGpsAutomatico
            )
        }

        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text("  Cerrar sesión")
        }
    }
}

@Composable
private fun FilaPreferencia(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    // El fondo de la tarjeta cambia de color con animación al activar/desactivar
    val fondo by animateColorAsState(
        if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(500),
        label = "fondoPreferencia"
    )
    Card(colors = CardDefaults.cardColors(containerColor = fondo), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(descripcion, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = activo, onCheckedChange = onCambio)
        }
    }
}
