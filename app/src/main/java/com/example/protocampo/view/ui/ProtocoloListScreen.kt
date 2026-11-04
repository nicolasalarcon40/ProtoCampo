package com.example.protocampo.view.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.repository.models.Catalogos
import com.example.protocampo.viewmodels.ProtocoloViewModel

@Composable
fun ProtocoloListScreen(
    viewModel: ProtocoloViewModel,
    onNuevo: () -> Unit,
    onVerProtocolo: (Int) -> Unit
) {
    val protocolos by viewModel.protocolos.collectAsStateWithLifecycle()
    var filtro by rememberSaveable { mutableStateOf("Todos") }

    // derivedStateOf: solo se recalcula cuando cambia el filtro o la lista
    val filtrados by remember(protocolos) {
        derivedStateOf {
            if (filtro == "Todos") protocolos else protocolos.filter { it.protocolo.resultado == filtro }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (listOf("Todos") + Catalogos.resultados).forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { filtro = opcion },
                        label = { Text(opcion) }
                    )
                }
            }
            if (filtrados.isEmpty()) {
                Text(
                    "No hay protocolos para mostrar.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtrados, key = { it.protocolo.id }) { item ->
                    FilaProtocolo(item) { onVerProtocolo(item.protocolo.id) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
            text = { Text("Nuevo protocolo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}
