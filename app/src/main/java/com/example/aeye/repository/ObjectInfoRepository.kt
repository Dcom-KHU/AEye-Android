package com.example.aeye.repository

import androidx.annotation.WorkerThread
import com.example.aeye.database.ObjectInfoDao
import com.example.aeye.model.ObjectInfo
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
    fun findByClassName(className: String) : ObjectInfo {
        return objectInfoDao.findByClassName(className)
    }

}

