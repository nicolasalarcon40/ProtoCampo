package com.example.protocampo.navegations

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.protocampo.view.ui.AjustesScreen
import com.example.protocampo.view.ui.EquipoDetalleScreen
import com.example.protocampo.view.ui.EquipoFormScreen
import com.example.protocampo.view.ui.EquipoListScreen
import com.example.protocampo.view.ui.HomeScreen
import com.example.protocampo.view.ui.LoginScreen
import com.example.protocampo.view.ui.ProtocoloDetalleScreen
import com.example.protocampo.view.ui.ProtocoloFormScreen
import com.example.protocampo.view.ui.ProtocoloListScreen
import com.example.protocampo.view.utils.navEnterTransition
import com.example.protocampo.view.utils.navExitTransition
import com.example.protocampo.view.utils.navPopEnterTransition
import com.example.protocampo.view.utils.navPopExitTransition
import com.example.protocampo.viewmodels.AjustesViewModel
import com.example.protocampo.viewmodels.EquipoViewModel
import com.example.protocampo.viewmodels.LoginViewModel
import com.example.protocampo.viewmodels.ProtocoloViewModel

/** Rutas centralizadas de la app. */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROTOCOLOS = "protocolos"
    const val PROTOCOLO_FORM = "protocolo_form?id={id}&equipoId={equipoId}"
    const val PROTOCOLO_DETALLE = "protocolo/{id}"
    const val EQUIPOS = "equipos"
    const val EQUIPO_FORM = "equipo_form?id={id}"
    const val EQUIPO_DETALLE = "equipo/{id}"
    const val AJUSTES = "ajustes"

    fun protocoloForm(id: Int = 0, equipoId: Int = 0) = "protocolo_form?id=$id&equipoId=$equipoId"
    fun protocoloDetalle(id: Int) = "protocolo/$id"
    fun equipoForm(id: Int = 0) = "equipo_form?id=$id"
    fun equipoDetalle(id: Int) = "equipo/$id"
}

data class ItemMenu(val ruta: String, val etiqueta: String, val icono: ImageVector)

/** Destinos principales de la barra de navegación inferior. */
val itemsMenu = listOf(
    ItemMenu(Routes.HOME, "Inicio", Icons.Default.Home),
    ItemMenu(Routes.PROTOCOLOS, "Protocolos", Icons.Default.Assignment),
    ItemMenu(Routes.EQUIPOS, "Equipos", Icons.Default.ElectricalServices),
    ItemMenu(Routes.AJUSTES, "Ajustes", Icons.Default.Settings)
)

private fun tituloPara(ruta: String?): String = when (ruta) {
    Routes.HOME -> "ProtoCampo"
    Routes.PROTOCOLOS -> "Protocolos"
    Routes.PROTOCOLO_FORM -> "Protocolo de prueba"
    Routes.PROTOCOLO_DETALLE -> "Detalle del protocolo"
    Routes.EQUIPOS -> "Equipos"
    Routes.EQUIPO_FORM -> "Equipo"
    Routes.EQUIPO_DETALLE -> "Ficha del equipo"
    Routes.AJUSTES -> "Ajustes"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    equipoViewModel: EquipoViewModel,
    protocoloViewModel: ProtocoloViewModel,
    ajustesViewModel: AjustesViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val esLogin = rutaActual == null || rutaActual == Routes.LOGIN
    val esPrincipal = itemsMenu.any { it.ruta == rutaActual }

    fun irAPrincipal(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            if (!esLogin) {
                TopAppBar(
                    title = { Text(tituloPara(rutaActual)) },
                    navigationIcon = {
                        if (!esPrincipal) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            // Barra de navegación inferior (solo en las 4 pantallas principales)
            if (!esLogin && esPrincipal) {
                NavigationBar {
                    itemsMenu.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.ruta,
                            onClick = { irAPrincipal(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(item.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            loginViewModel = loginViewModel,
            equipoViewModel = equipoViewModel,
            protocoloViewModel = protocoloViewModel,
            ajustesViewModel = ajustesViewModel,
            nombreTecnico = loginState.tecnico?.nombre ?: "",
            onIrAPrincipal = ::irAPrincipal
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    equipoViewModel: EquipoViewModel,
    protocoloViewModel: ProtocoloViewModel,
    ajustesViewModel: AjustesViewModel,
    nombreTecnico: String,
    onIrAPrincipal: (String) -> Unit
) {
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    /** Vuelve atrás solo si seguimos en la ruta indicada (evita doble "atrás"). */
    fun volverDesde(ruta: String) {
        if (navController.currentBackStackEntry?.destination?.route == ruta) navController.popBackStack()
    }

    // ANIMACIÓN 3: deslizamiento horizontal entre pantallas
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = protocoloViewModel,
                nombreTecnico = nombreTecnico,
                onNuevoProtocolo = { navController.navigate(Routes.protocoloForm()) },
                onVerEquipos = { onIrAPrincipal(Routes.EQUIPOS) },
                onVerProtocolo = { navController.navigate(Routes.protocoloDetalle(it)) }
            )
        }
        composable(Routes.PROTOCOLOS) {
            ProtocoloListScreen(
                viewModel = protocoloViewModel,
                onNuevo = { navController.navigate(Routes.protocoloForm()) },
                onVerProtocolo = { navController.navigate(Routes.protocoloDetalle(it)) }
            )
        }
        composable(
            Routes.PROTOCOLO_FORM,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType; defaultValue = 0 },
                navArgument("equipoId") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            ProtocoloFormScreen(
                viewModel = protocoloViewModel,
                ajustesViewModel = ajustesViewModel,
                protocoloId = entry.arguments?.getInt("id") ?: 0,
                equipoIdInicial = entry.arguments?.getInt("equipoId") ?: 0,
                tecnicoNombre = nombreTecnico,
                onListo = { volverDesde(Routes.PROTOCOLO_FORM) }
            )
        }
        composable(
            Routes.PROTOCOLO_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            ProtocoloDetalleScreen(
                viewModel = protocoloViewModel,
                protocoloId = entry.arguments?.getInt("id") ?: 0,
                onEditar = { navController.navigate(Routes.protocoloForm(id = it)) },
                onEliminado = { volverDesde(Routes.PROTOCOLO_DETALLE) }
            )
        }
        composable(Routes.EQUIPOS) {
            EquipoListScreen(
                viewModel = equipoViewModel,
                onNuevo = { navController.navigate(Routes.equipoForm()) },
                onVerEquipo = { navController.navigate(Routes.equipoDetalle(it)) }
            )
        }
        composable(
            Routes.EQUIPO_FORM,
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = 0 })
        ) { entry ->
            EquipoFormScreen(
                viewModel = equipoViewModel,
                equipoId = entry.arguments?.getInt("id") ?: 0,
                onListo = { volverDesde(Routes.EQUIPO_FORM) }
            )
        }
        composable(
            Routes.EQUIPO_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            EquipoDetalleScreen(
                equipoViewModel = equipoViewModel,
                protocoloViewModel = protocoloViewModel,
                equipoId = entry.arguments?.getInt("id") ?: 0,
                onEditar = { navController.navigate(Routes.equipoForm(it)) },
                onNuevoProtocolo = { navController.navigate(Routes.protocoloForm(equipoId = it)) },
                onVerProtocolo = { navController.navigate(Routes.protocoloDetalle(it)) },
                onEliminado = { volverDesde(Routes.EQUIPO_DETALLE) }
            )
        }
        composable(Routes.AJUSTES) {
            AjustesScreen(
                viewModel = ajustesViewModel,
                tecnico = loginState.tecnico,
                onCerrarSesion = {
                    loginViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
