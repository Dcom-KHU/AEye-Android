package com.example.aeye.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "object_info")
data class ObjectInfo(
    @ColumnInfo(name = "class_name") var className : String,
    @ColumnInfo(name = "object_name") var objectName : String,
    @ColumnInfo(name = "medicine_info") var info : String
){
    @PrimaryKey(autoGenerate = true) var id: Int = 0
    constructor(): this("", "", "")
}
