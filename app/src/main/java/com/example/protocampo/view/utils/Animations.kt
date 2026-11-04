package com.example.protocampo.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry

/*
 * Animaciones de la app reunidas en un solo lugar:
 *   ANIMACIÓN 1: AnimacionEntradaFormulario -> formularios (fade + deslizamiento)
 *   ANIMACIÓN 2: Modifier.pulso()           -> logo del Login (animación infinita)
 *   ANIMACIÓN 3: transiciones del NavHost   -> deslizamiento entre pantallas
 *   ANIMACIÓN 4: AparecerSuave              -> mensajes de error / éxito / medición anterior
 * Además, en las pantallas se usan:
 *   animateColorAsState  -> color del resultado (Aprobado/Observado/Rechazado) y switch de tema
 *   animateIntAsState    -> contadores del Inicio
 *   animateContentSize   -> tarjetas que cambian de tamaño
 */

// ANIMACIÓN 1 ----------------------------------------------------------------
@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
        content = content
    )
}

// ANIMACIÓN 2 ----------------------------------------------------------------
@Composable
fun Modifier.pulso(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulso")
    val escala by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escala"
    )
    return this.graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}

// ANIMACIÓN 3 ----------------------------------------------------------------
private const val DURACION_NAV = 350

val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}

val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}

// ANIMACIÓN 4 ----------------------------------------------------------------
/** Aparece creciendo y desaparece encogiéndose (para mensajes y tarjetas). */
@Composable
fun AparecerSuave(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)) + scaleIn(tween(300), initialScale = 0.9f),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)) + scaleOut(tween(200), targetScale = 0.9f),
        content = content
    )
}
