package com.example.skillbridge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedJobEntity::class, CachedCategoryEntity::class, OfflineActionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SkillBridgeDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao
    abstract fun categoryDao(): CategoryDao
    abstract fun offlineActionDao(): OfflineActionDao

    companion object {
        @Volatile
        private var INSTANCE: SkillBridgeDatabase? = null

        fun getInstance(context: Context): SkillBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkillBridgeDatabase::class.java,
                    "skillbridge_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
