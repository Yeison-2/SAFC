package com.safc.caficultura.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safc.caficultura.data.local.dao.EmpleadoDao
import com.safc.caficultura.data.local.dao.PagoDao
import com.safc.caficultura.data.local.dao.ProduccionDao
import com.safc.caficultura.data.local.dao.UsuarioDao
import com.safc.caficultura.data.local.entity.Empleado
import com.safc.caficultura.data.local.entity.Pago
import com.safc.caficultura.data.local.entity.Produccion
import com.safc.caficultura.data.local.entity.Usuario

@Database(
    entities = [
        Empleado::class,
        Usuario::class,
        Produccion::class,
        Pago::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SafcDatabase : RoomDatabase() {

    abstract fun empleadoDao(): EmpleadoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun produccionDao(): ProduccionDao
    abstract fun pagoDao(): PagoDao

    companion object {
        fun crear(context: Context): SafcDatabase {
            return Room.databaseBuilder(context, SafcDatabase::class.java, "safc.db")
                .addCallback(SemillaAdministradorCallback)
                .fallbackToDestructiveMigration()
                .build()
        }

        private val SemillaAdministradorCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertarAdmin(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                insertarAdmin(db)
            }

            private fun insertarAdmin(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO usuarios (usuario, contrasena, rol, empleadoId)
                    VALUES ('superadmin', 'super123', 'SUPER_ADMIN', NULL)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO usuarios (usuario, contrasena, rol, empleadoId)
                    VALUES ('admin', 'admin123', 'ADMIN', NULL)
                    """.trimIndent()
                )
            }
        }
    }
}
