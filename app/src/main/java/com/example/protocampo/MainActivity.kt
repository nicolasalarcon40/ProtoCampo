package com.example.protocampo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protocampo.navegations.AppNavigation
import com.example.protocampo.view.theme.ProtoCampoTheme
import com.example.protocampo.viewmodels.AjustesViewModel
import com.example.protocampo.viewmodels.EquipoViewModel
import com.example.protocampo.viewmodels.LoginViewModel
import com.example.protocampo.viewmodels.ProtocoloViewModel

class MainActivity : ComponentActivity() {

    // ViewModels con alcance de la Activity: UNA sola instancia compartida por todas las pantallas
    private val loginViewModel: LoginViewModel by viewModels()
    private val equipoViewModel: EquipoViewModel by viewModels()
    private val protocoloViewModel: ProtocoloViewModel by viewModels()
    private val ajustesViewModel: AjustesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Tema oscuro guardado en DataStore (null mientras carga → tema del sistema)
            val temaOscuro by ajustesViewModel.temaOscuro.collectAsStateWithLifecycle()

            ProtoCampoTheme(temaOscuro = temaOscuro ?: isSystemInDarkTheme()) {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    equipoViewModel = equipoViewModel,
                    protocoloViewModel = protocoloViewModel,
                    ajustesViewModel = ajustesViewModel
                )
            }
        }
    }
}
