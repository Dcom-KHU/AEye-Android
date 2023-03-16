package com.example.aeye.database

import androidx.room.*
import com.example.aeye.model.ObjectInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectInfoDao {
    @Insert
    suspend fun insertData(objectInfo: ObjectInfo)

    @Insert
    suspend fun insertPreliminaryData(objectInfoList: List<ObjectInfo>)

    @Query("SELECT * FROM object_info WHERE class_name = :category")
    fun findByClassName(category: String): ObjectInfo

    @Delete
    suspend fun deleteData(objectInfo: ObjectInfo)

    @Query("SELECT * FROM object_info ORDER BY class_name ASC")
    fun getAllData() : Flow<List<ObjectInfo>>

}