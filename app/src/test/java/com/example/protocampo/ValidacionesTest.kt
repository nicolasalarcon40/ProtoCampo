package com.example.protocampo

import com.example.protocampo.viewmodels.ProtocoloViewModel
import com.example.protocampo.viewmodels.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas unitarias de la lógica de validación (no necesitan emulador).
 * Ejecutar: clic derecho sobre la clase > Run 'ValidacionesTest'.
 */
class ValidacionesTest {

    @Test
    fun email_valido_y_invalido() {
        assertNull(Validaciones.email("tecnico@induztek.cl"))
        assertNotNull(Validaciones.email(""))
        assertNotNull(Validaciones.email("tecnico.induztek.cl"))
    }

    @Test
    fun codigo_de_equipo() {
        assertNull(Validaciones.codigoEquipo("TR-001"))
        assertNotNull(Validaciones.codigoEquipo("TR001"))
        assertNotNull(Validaciones.codigoEquipo("tr-001"))
    }

    @Test
    fun numero_acepta_coma_decimal_y_valida_rango() {
        assertEquals(12.5, Validaciones.aNumero("12,5")!!, 0.0001)
        assertNull(Validaciones.numeroEnRango("25", "La temperatura", -20.0, 60.0))
        assertNotNull(Validaciones.numeroEnRango("80", "La temperatura", -20.0, 60.0))
        assertNotNull(Validaciones.numeroEnRango("abc", "La temperatura", -20.0, 60.0))
    }

    @Test
    fun observacion_obligatoria_si_no_esta_aprobado() {
        assertNull(Validaciones.observaciones("", "Aprobado"))
        assertNotNull(Validaciones.observaciones("", "Rechazado"))
        assertNull(Validaciones.observaciones("Aislamiento bajo en fase R", "Rechazado"))
    }

    @Test
    fun variacion_porcentual_respecto_a_medicion_anterior() {
        assertEquals(-12.43, ProtocoloViewModel.variacion(1620.0, 1850.0)!!, 0.01)
        assertNull(ProtocoloViewModel.variacion(1620.0, null))
    }
}
