package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VehicleEntity::class, AlertEntity::class, GeofenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TharaDatabase : RoomDatabase() {
    abstract fun fleetDao(): FleetDao

    companion object {
        @Volatile
        private var INSTANCE: TharaDatabase? = null

        fun getDatabase(context: Context): TharaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TharaDatabase::class.java,
                    "thara_tracking_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
