package com.example.aeye.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectInfoDao {
    @Insert
    suspend fun insert(objectInfo: ObjectInfo)

    @Query("SELECT * FROM object_info WHERE class_name LIKE :category LIMIT 1")
    suspend fun findByClassName(category: String): ObjectInfo

    @Delete
    suspend fun delete(objectInfo: ObjectInfo)

    @Query("SELECT * FROM object_info ORDER BY class_name ASC")
    fun getAll() : Flow<List<ObjectInfo>>

}