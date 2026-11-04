# ProtoCampo — Protocolos de prueba eléctrica en terreno

App Android (Kotlin + Jetpack Compose + Material 3) con arquitectura **MVVM**, persistencia local con **Room/SQLite** y **DataStore**, **navegación**, diseño **Compact** para teléfono, **animaciones** y dos **recursos nativos** (cámara y GPS).

**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles — Duoc UC
**Estudiante:** Nicolás Alarcón Ferrus
**Equipo N°:** 16A
**Caso:** Induztek Ingeniería SpA — Digitalización del protocolo de pruebas eléctricas en terreno

## Problema y solución

Hoy los técnicos de Induztek anotan los resultados de cada prueba en papel y recién los traspasan a Excel/Word al volver a la oficina. Eso provoca doble digitación, errores de traspaso y demora en la entrega del protocolo al cliente.

**ProtoCampo** permite registrar la prueba directamente en terreno, **sin internet** (todo se guarda en el teléfono), compararla automáticamente con las mediciones anteriores del mismo equipo y **enviar el protocolo al cliente en el momento** (correo, WhatsApp, etc.).

## Funcionalidades

| Funcionalidad | Detalle |
|---|---|
| Login | Validación de correo y contraseña, opción "Recordar mi correo" y sesión persistente (DataStore) |
| Inicio | Contadores animados, accesos rápidos y últimos protocolos |
| Equipos (CRUD) | Crear, listar, buscar, editar y eliminar equipos. Código único (ej. TR-001) y foto de la placa |
| Protocolos (CRUD) | Registrar prueba: equipo, tipo de prueba, valor medido, tensión, temperatura, resultado, observaciones, GPS y foto de evidencia |
| Historial y comparación | Ficha del equipo con todas sus pruebas y la **variación %** respecto de la medición anterior del mismo tipo |
| Entrega inmediata | Botón "Enviar protocolo al cliente" (Intent de compartir) y "Ver ubicación en el mapa" |
| Ajustes | Tema oscuro y GPS automático, guardados con DataStore |

## Cumplimiento de requisitos (EP2)

| Requisito | Dónde está |
|---|---|
| Diseño visual Material 3 | `view/theme/` (Color, Type, Theme), `TopAppBar`, `NavigationBar`, `Card`, `FilterChip`, `AlertDialog` |
| Pantalla Compact (solo teléfono) | Todas las pantallas están diseñadas para celular en vertical: una columna con scroll y barra de navegación inferior (`AppNavigation`) |
| Formularios validados con íconos | `view/utils/Componentes.kt` (`CampoTexto`, `SelectorLista`): ícono, borde rojo, ícono de error y mensaje por campo |
| Validaciones desacopladas de la UI | `viewmodels/Validaciones.kt` + `validar()` de cada ViewModel; la vista solo muestra los errores del `UiState` |
| Navegación | `navegations/AppNavigation.kt`: `NavHost`, rutas con argumentos (`equipo/{id}`, `protocolo_form?id=&equipoId=`) |
| Gestión de estado | `StateFlow` + `collectAsStateWithLifecycle`, `rememberSaveable`, `derivedStateOf` (búsqueda y filtros), ViewModels compartidos |
| Almacenamiento local SQLite | `repository/data/` (Room): `AppDatabase`, `EquipoDao`, `ProtocoloDao`, `TecnicoDao`; FK protocolo → equipo con CASCADE |
| DataStore | `repository/data/PreferenciasDataStore.kt` (tema oscuro, sesión, correo recordado, GPS automático) |
| Recurso nativo 1: Cámara | `view/utils/CameraCapture.kt` (CameraX + permiso en tiempo de ejecución): foto de placa y evidencia |
| Recurso nativo 2: GPS | `view/utils/UbicacionGps.kt` (FusedLocationProviderClient + permisos): ubicación de la prueba |
| Animaciones | `view/utils/Animations.kt` (entrada de formularios, pulso del logo, transiciones del NavHost, aparición de mensajes), `animateColorAsState`, `animateIntAsState`, `animateContentSize` |
| Pruebas unitarias | `app/src/test/.../ValidacionesTest.kt` |

## Estructura (MVVM)

```
app/src/main/java/com/example/protocampo/
├── MainActivity.kt                 Punto de entrada y ViewModels compartidos
├── repository/                     MODEL
│   ├── models/   Tecnico, Equipo, Protocolo (+ProtocoloConEquipo), Catalogos
│   └── data/     AppDatabase, TecnicoDao, EquipoDao, ProtocoloDao, PreferenciasDataStore
├── viewmodels/                     VIEWMODEL
│   ├── LoginViewModel, EquipoViewModel, ProtocoloViewModel, AjustesViewModel
│   ├── Validaciones.kt             Reglas de validación centralizadas
│   └── ProtocoloTexto.kt           Formato de fechas, números y texto para compartir
├── navegations/
│   └── AppNavigation.kt            Rutas, barra de navegación inferior y NavHost
└── view/                           VIEW
    ├── theme/    Color.kt, Type.kt, Theme.kt
    ├── utils/    Componentes, CameraCapture, UbicacionGps, PhotoBox, Animations
    └── ui/       Login, Home, EquipoList, EquipoForm, EquipoDetalle,
                  ProtocoloList, ProtocoloForm, ProtocoloDetalle, Ajustes
```

## Cómo ejecutar

1. Android Studio → **Open** → carpeta `ProtoCampo`.
2. Esperar el **Gradle Sync** (Gradle 8.13, AGP 8.13.1, Kotlin 2.0.21, JDK 17 — mismas versiones del proyecto de ejemplo del curso). Si Android Studio ofrece actualizar el Android Gradle Plugin, **no aceptar**.
3. Ejecutar en un emulador o teléfono con Android 8.0+ (minSdk 26).
4. Ingresar con el usuario de prueba: **tecnico@induztek.cl / tecnico123**.
5. La app trae datos **ficticios** de ejemplo (2 equipos y 3 protocolos) para ver el historial y la comparación.

**En el emulador:** la cámara trasera muestra una escena virtual, y la ubicación se define en *Extended controls (⋯) → Location → Set location*.

**Pruebas unitarias:** clic derecho en `app/src/test/java/com/example/protocampo/ValidacionesTest.kt` → *Run*.

## Notas

- Todos los datos son ficticios (requisito del caso).
- Las fotos se guardan en el almacenamiento interno privado de la app (`files/fotos/equipos` y `files/fotos/protocolos`), no aparecen en la galería y se excluyen del respaldo.
- La integración con backend (Spring Boot + API REST + Retrofit) indicada en el caso corresponde a la etapa final (EFT).
