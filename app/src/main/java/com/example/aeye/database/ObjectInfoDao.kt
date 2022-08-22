package com.example.aeye.database

import androidx.room.*

@Dao
interface ObjectInfoDao {
    @Insert
    fun insert(objectInfo: ObjectInfo)

    @Query("SELECT * FROM ObjectInfo WHERE class_name LIKE :category LIMIT 1")
    fun findByClassName(category : String) : ObjectInfo

    @Delete
    fun delete(objectInfo: ObjectInfo)
}