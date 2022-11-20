package com.example.aeye.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [ObjectInfo::class], version = 1)
abstract class ObjectInfoDatabase: RoomDatabase() {
    abstract fun objectInfoDao() : ObjectInfoDao

    private class ObjectInfoDatabaseCallback(private val scope : CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            instance?.let {
                scope.launch {
                    for (info : ObjectInfo in initDatabase()){
                        it.objectInfoDao().insert(info)
                    }
                }
            }
        }
    }

    companion object {
        private var instance: ObjectInfoDatabase? = null

        @Synchronized
        fun getDataBase(context: Context, scope: CoroutineScope) : ObjectInfoDatabase? {
            if (instance == null){
                synchronized(this){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ObjectInfoDatabase::class.java,
                        "object-info-database"
                    ).addCallback(ObjectInfoDatabaseCallback(scope))
                        .build()
                }
            }
            return instance
        }
    }
}