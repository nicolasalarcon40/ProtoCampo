package com.example.protocampo.view.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

/**
 * RECURSO NATIVO 2: GPS.
 * 1) Pide los permisos de ubicación en tiempo de ejecución.
 * 2) Obtiene la posición actual con FusedLocationProviderClient.
 * 3) Entrega latitud/longitud al ViewModel mediante onUbicacion.
 * Si el usuario niega el permiso o el GPS está apagado, muestra un mensaje amigable (no se cae).
 */
@Composable
fun TarjetaUbicacion(
    latitud: Double?,
    longitud: Double?,
    automatico: Boolean,
    onUbicacion: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var buscando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun obtener() {
        buscando = true
        mensaje = null
        obtenerUbicacionActual(
            context = context,
            onExito = { lat, lon ->
                buscando = false
                onUbicacion(lat, lon)
            },
            onError = { error ->
                buscando = false
                mensaje = error
            }
        )
    }

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) obtener()
        else mensaje = "Permiso de ubicación denegado. Puedes guardar el protocolo sin GPS."
    }

    fun solicitar() {
        if (tienePermisoUbicacion(context)) obtener()
        else permisosLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Si está activado en Ajustes (DataStore), captura la ubicación al abrir el formulario
    LaunchedEffect(automatico) {
        if (automatico && latitud == null) solicitar()
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (latitud != null) Icons.Default.LocationOn else Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = if (latitud != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "  Ubicación de la instalación (GPS)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (latitud != null && longitud != null) {
                Text(
                    String.format(Locale.US, "Lat: %.5f   Lon: %.5f", latitud, longitud),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text("Sin ubicación registrada", style = MaterialTheme.typography.bodyMedium)
            }
            AparecerSuave(visible = mensaje != null) {
                Text(mensaje ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { solicitar() }, enabled = !buscando, modifier = Modifier.fillMaxWidth()) {
                if (buscando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("  Buscando señal GPS...")
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Text(if (latitud == null) "  Obtener ubicación" else "  Actualizar ubicación")
                }
            }
        }
    }
}

fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission") // el permiso se revisa antes de llamar
private fun obtenerUbicacionActual(
    context: Context,
    onExito: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    if (!tienePermisoUbicacion(context)) {
        onError("Falta el permiso de ubicación")
        return
    }
    try {
        val cliente = LocationServices.getFusedLocationProviderClient(context)
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) onExito(ubicacion.latitude, ubicacion.longitude)
                else onError("No se pudo obtener la ubicación. Revisa que el GPS esté encendido.")
            }
            .addOnFailureListener { onError("Error de GPS: ${it.message}") }
    } catch (e: SecurityException) {
        onError("Permiso de ubicación no disponible")
    }
}
