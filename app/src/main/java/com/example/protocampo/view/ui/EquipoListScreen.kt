package com.example.protocampo.view.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.protocampo.repository.models.Equipo
import com.example.protocampo.viewmodels.EquipoViewModel

@Composable
fun EquipoListScreen(
    viewModel: EquipoViewModel,
    onNuevo: () -> Unit,
    onVerEquipo: (Int) -> Unit
) {
    val equipos by viewModel.equipos.collectAsStateWithLifecycle()
    var busqueda by rememberSaveable { mutableStateOf("") }

    // derivedStateOf: la lista filtrada solo se recalcula si cambia la búsqueda o la lista
    val filtrados by remember(equipos) {
        derivedStateOf {
            val texto = busqueda.trim()
            if (texto.isEmpty()) equipos
            else equipos.filter {
                it.codigo.contains(texto, ignoreCase = true) ||
                    it.cliente.contains(texto, ignoreCase = true) ||
                    it.tipo.contains(texto, ignoreCase = true)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por código, cliente o tipo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            if (filtrados.isEmpty()) {
                Text(
                    if (equipos.isEmpty()) "No hay equipos. Agrega el primero con el botón +." else "Sin resultados.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtrados, key = { it.id }) { equipo ->
                    TarjetaEquipo(equipo) { onVerEquipo(equipo.id) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Nuevo equipo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun TarjetaEquipo(equipo: Equipo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (equipo.fotoUri != null) {
                    AsyncImage(
                        model = equipo.fotoUri,
                        contentDescription = "Foto de ${equipo.codigo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.ElectricalServices, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(equipo.codigo, style = MaterialTheme.typography.titleMedium)
                Text(equipo.tipo, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${equipo.cliente} · ${equipo.instalacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
