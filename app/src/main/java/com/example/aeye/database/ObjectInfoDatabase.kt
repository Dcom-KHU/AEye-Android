package com.example.aeye.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ObjectInfo::class], version = 1)
abstract class ObjectInfoDatabase: RoomDatabase() {
    abstract fun objectInfoDao() : ObjectInfoDao

    companion object {
        private var instance: ObjectInfoDatabase? = null

        @Synchronized
        fun getInstance(context: Context) : ObjectInfoDatabase? {
            if (instance == null){
                synchronized(ObjectInfoDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ObjectInfoDatabase::class.java,
                        "object-info-database"
                    )
                        .build()
                }
            }
            return instance
        }
    }
}