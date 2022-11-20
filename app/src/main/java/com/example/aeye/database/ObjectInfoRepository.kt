package com.example.aeye.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ObjectInfoRepository(private val objectInfoDao: ObjectInfoDao){

    val allObjects : Flow<List<ObjectInfo>> = objectInfoDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(newInfo: ObjectInfo){
        objectInfoDao.insert(newInfo)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(nonInfo: ObjectInfo){
        objectInfoDao.delete(nonInfo)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun findByClassName(className: String) : ObjectInfo{
        return objectInfoDao.findByClassName(className)
    }

}

