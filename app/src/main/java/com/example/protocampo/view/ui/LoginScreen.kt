package com.example.protocampo.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.view.utils.AparecerSuave
import com.example.protocampo.view.utils.CampoTexto
import com.example.protocampo.view.utils.pulso
import com.example.protocampo.viewmodels.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginExitoso: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    // Estado local de la vista: solo afecta lo que se ve (no es lógica de negocio)
    var verClave by rememberSaveable { mutableStateOf(false) }

    // Si hay sesión (guardada en DataStore o recién iniciada) se navega al Inicio
    LaunchedEffect(estado.tecnico) {
        if (estado.tecnico != null) onLoginExitoso()
    }

    if (estado.verificandoSesion) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Bolt,
            contentDescription = "Logo ProtoCampo",
            tint = MaterialTheme.colorScheme.secondary,
            // ANIMACIÓN 2: pulso infinito del logo
            modifier = Modifier.size(96.dp).pulso()
        )
        Text("ProtoCampo", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Protocolos de prueba en terreno · Induztek", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        CampoTexto(
            valor = estado.email,
            onValorChange = viewModel::onEmailChange,
            etiqueta = "Correo",
            icono = Icons.Default.Email,
            error = estado.errores.email,
            tipoTeclado = KeyboardType.Email
        )
        CampoTexto(
            valor = estado.password,
            onValorChange = viewModel::onPasswordChange,
            etiqueta = "Contraseña",
            icono = Icons.Default.Lock,
            error = estado.errores.password,
            tipoTeclado = KeyboardType.Password,
            transformacion = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
            iconoDerecho = {
                IconButton(onClick = { verClave = !verClave }) {
                    Icon(
                        if (verClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (verClave) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = estado.recordarEmail, onCheckedChange = viewModel::onRecordarEmailChange)
            Text("Recordar mi correo")
        }

        AparecerSuave(visible = estado.errorGeneral != null) {
            Text(
                estado.errorGeneral ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::login,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Ingresar")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Usuario de prueba: tecnico@induztek.cl / tecnico123", style = MaterialTheme.typography.bodySmall)
    }
}
