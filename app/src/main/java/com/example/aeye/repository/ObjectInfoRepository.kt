package com.example.aeye.repository

import androidx.annotation.WorkerThread
import com.example.aeye.database.ObjectInfoDao
import com.example.aeye.model.ObjectInfo
import kotlinx.coroutines.flow.Flow

class ObjectInfoRepository(private val objectInfoDao: ObjectInfoDao){

    val allObjects : Flow<List<ObjectInfo>> = objectInfoDao.getAllData()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(newInfo: ObjectInfo){
        objectInfoDao.insertData(newInfo)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(nonInfo: ObjectInfo){
        objectInfoDao.deleteData(nonInfo)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun findByClassName(className: String) : ObjectInfo {
        return objectInfoDao.findByClassName(className)
    }

}

