package com.example.aeye.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ObjectInfo(
    @ColumnInfo(name = "class_name") var className : String,
    var objectName : String,
    var info : String
){
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
