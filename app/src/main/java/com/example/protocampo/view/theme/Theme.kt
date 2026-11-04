package com.example.protocampo.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresClaros = lightColorScheme(
    primary = AzulInduztek,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = AmbarSeguridad,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFE0B2),
    tertiary = GrisAcero
)

private val ColoresOscuros = darkColorScheme(
    primary = AzulClaro,
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF15386E),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = AmbarClaro,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5D4200),
    tertiary = GrisAceroClaro
)

/** Tema Material 3 de la app. El modo oscuro se guarda en DataStore (pantalla Ajustes). */
@Composable
fun ProtoCampoTheme(temaOscuro: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (temaOscuro) ColoresOscuros else ColoresClaros,
        typography = Tipografia,
        content = content
    )
}
