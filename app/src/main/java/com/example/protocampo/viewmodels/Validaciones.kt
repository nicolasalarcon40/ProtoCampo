package com.example.protocampo.viewmodels

/**
 * Reglas de validación CENTRALIZADAS (desacopladas de la interfaz).
 * Cada función devuelve null si el valor es válido, o el mensaje de error.
 * Son funciones puras: se pueden probar con pruebas unitarias (ver ValidacionesTest).
 */
object Validaciones {

    private val EMAIL = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

    /** Código de equipo: 2 a 4 letras, guion y 3 a 5 números. Ej: TR-001, RP-0142 */
    private val CODIGO_EQUIPO = Regex("^[A-Z]{2,4}-\\d{3,5}$")

    fun email(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !EMAIL.matches(valor.trim()) -> "Formato de correo inválido (ej: nombre@empresa.cl)"
        else -> null
    }

    fun password(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Debe tener al menos 6 caracteres"
        else -> null
    }

    fun obligatorio(valor: String, campo: String, minimo: Int = 3): String? = when {
        valor.isBlank() -> "Este campo es obligatorio"
        valor.trim().length < minimo -> "$campo debe tener al menos $minimo caracteres"
        else -> null
    }

    fun codigoEquipo(valor: String): String? = when {
        valor.isBlank() -> "El código es obligatorio"
        !CODIGO_EQUIPO.matches(valor.trim()) -> "Formato: letras-números (ej: TR-001)"
        else -> null
    }

    fun seleccion(valor: String?, campo: String): String? =
        if (valor.isNullOrBlank()) "Selecciona $campo" else null

    /** Convierte "12,5" o "12.5" a Double (los técnicos usan coma decimal). */
    fun aNumero(valor: String): Double? = valor.trim().replace(',', '.').toDoubleOrNull()

    /** Número obligatorio dentro de un rango [min, max]. */
    fun numeroEnRango(valor: String, campo: String, min: Double, max: Double): String? {
        val numero = aNumero(valor)
        return when {
            valor.isBlank() -> "Este campo es obligatorio"
            numero == null -> "$campo debe ser un número"
            numero < min || numero > max -> "$campo debe estar entre ${formato(min)} y ${formato(max)}"
            else -> null
        }
    }

    /** Dependencia entre campos: si el resultado NO es "Aprobado", la observación es obligatoria. */
    fun observaciones(valor: String, resultado: String): String? =
        if (resultado != "Aprobado" && valor.trim().length < 10)
            "Explica el motivo (mín. 10 caracteres) cuando el resultado es $resultado"
        else null

    private fun formato(n: Double): String =
        if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
}
