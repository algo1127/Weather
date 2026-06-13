package com.algo1127.weather.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class], version = 2, exportSchema = false) // 🆕 Version 2
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "potato_weather_database"
                )
                    .fallbackToDestructiveMigration() // 🆕 Evita crashes al cambiar la versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}