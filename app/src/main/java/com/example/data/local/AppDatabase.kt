package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Resident
import com.example.data.sample.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(entities = [Resident::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun residentDao(): ResidentDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE residents ADD COLUMN fotoPenduduk TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE residents ADD COLUMN fotoKtp TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE residents ADD COLUMN fotoKk TEXT NOT NULL DEFAULT ''")
      }
    }

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "dapenda_population.db"
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .addCallback(DatabaseCallback(scope))
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch {
            populateDatabase(database.residentDao())
          }
        }
      }

      suspend fun populateDatabase(dao: ResidentDao) {
        // Database baru dibuat dalam kondisi bersih (0 data) tanpa data contoh
      }
    }
  }
}
