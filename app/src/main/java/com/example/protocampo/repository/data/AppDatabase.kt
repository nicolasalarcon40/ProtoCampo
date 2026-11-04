package com.example.protocampo.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.protocampo.repository.models.Equipo
import com.example.protocampo.repository.models.Protocolo
import com.example.protocampo.repository.models.Tecnico

/**
 * Base de datos local SQLite (Room). Funciona sin internet,
 * algo clave porque en terreno la conectividad es limitada.
 */
@Database(
    entities = [Tecnico::class, Equipo::class, Protocolo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tecnicoDao(): TecnicoDao
    abstract fun equipoDao(): EquipoDao
    abstract fun protocoloDao(): ProtocoloDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protocampo_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(datosIniciales)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Datos FICTICIOS de ejemplo (el caso prohíbe usar datos reales de clientes).
         * Se insertan solo la primera vez que se crea la BD (onCreate).
         */
        private val datosIniciales = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO tecnicos (nombre, email, password, cargo) VALUES " +
                        "('Técnico Demo', 'tecnico@induztek.cl', 'tecnico123', 'Técnico de terreno')"
                )
                db.execSQL(
                    "INSERT INTO equipos (id, codigo, tipo, marca, cliente, instalacion, fotoUri) VALUES " +
                        "(1, 'TR-001', 'Transformador de poder', 'Siemens', 'Minera Ficticia S.A.', 'S/E Norte, Antofagasta', NULL), " +
                        "(2, 'RP-014', 'Relé de protección', 'SEL', 'Planta Demo Ltda.', 'Sala eléctrica 2, Quilicura', NULL)"
                )
                // Historial de TR-001 para mostrar la comparación con mediciones anteriores
                db.execSQL(
                    "INSERT INTO protocolos (equipoId, tipoPrueba, valorMedido, unidad, tensionPruebaKv, temperaturaC, " +
                        "resultado, observaciones, fechaHora, latitud, longitud, fotoUri, tecnicoNombre) VALUES " +
                        "(1, 'Resistencia de aislamiento', 1850, 'MΩ', 5, 22, 'Aprobado', 'Sin novedades', 1741600800000, NULL, NULL, NULL, 'Técnico Demo'), " +
                        "(1, 'Resistencia de aislamiento', 1620, 'MΩ', 5, 24, 'Aprobado', 'Leve baja respecto a marzo', 1757329200000, NULL, NULL, NULL, 'Técnico Demo'), " +
                        "(2, 'Tiempo de operación', 42, 'ms', 0, 21, 'Aprobado', 'Disparo dentro de rango', 1754046000000, NULL, NULL, NULL, 'Técnico Demo')"
                )
            }
        }
    }
}
