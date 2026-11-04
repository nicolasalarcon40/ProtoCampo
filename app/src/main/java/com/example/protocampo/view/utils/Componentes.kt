package com.example.protocampo.view.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.protocampo.view.theme.AmarilloObservado
import com.example.protocampo.view.theme.RojoRechazado
import com.example.protocampo.view.theme.VerdeAprobado

/**
 * Campo de texto reutilizable con validación visual:
 * ícono a la izquierda, ícono de error a la derecha, borde rojo y mensaje bajo el campo.
 * NO valida nada: solo muestra el error que le entrega el ViewModel.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    sufijo: String? = null,
    ayuda: String? = null,
    unaLinea: Boolean = true,
    transformacion: VisualTransformation = VisualTransformation.None,
    iconoDerecho: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (error != null) {
            { Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
        } else {
            iconoDerecho
        },
        suffix = if (sufijo != null) {
            { Text(sufijo) }
        } else {
            null
        },
        isError = error != null,
        supportingText = {
            when {
                error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                ayuda != null -> Text(ayuda)
            }
        },
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = modifier.fillMaxWidth()
    )
}

/** Selector desplegable (ExposedDropdownMenu de Material 3) con validación visual. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorLista(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: String,
    onSeleccion: (String) -> Unit,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            isError = error != null,
            supportingText = { if (error != null) Text(error, color = MaterialTheme.colorScheme.error) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

fun colorResultado(resultado: String): Color = when (resultado) {
    "Aprobado" -> VerdeAprobado
    "Observado" -> AmarilloObservado
    "Rechazado" -> RojoRechazado
    else -> Color.Gray
}

fun iconoResultado(resultado: String): ImageVector = when (resultado) {
    "Aprobado" -> Icons.Default.CheckCircle
    "Observado" -> Icons.Default.Warning
    else -> Icons.Default.Error
}

/** Etiqueta de color según el resultado. El color cambia con animación (animateColorAsState). */
@Composable
fun ChipResultado(resultado: String) {
    val color by animateColorAsState(colorResultado(resultado), tween(500), label = "colorResultado")
    Surface(color = color.copy(alpha = 0.15f), contentColor = color, shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(iconoResultado(resultado), contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" $resultado", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Mensaje de éxito que aparece con animación. */
@Composable
fun MensajeExito(visible: Boolean, texto: String) {
    AparecerSuave(visible = visible) {
        Card(
            colors = CardDefaults.cardColors(containerColor = VerdeAprobado, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Text("  $texto", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

