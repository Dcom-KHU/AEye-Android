package com.example.aeye.application

import android.app.Application
import com.example.aeye.database.ObjectInfoDatabase
import com.example.aeye.repository.ObjectInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DetectApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    /**database가 처음 필요할 때 생성**/
    private val objectDatabase by lazy { ObjectInfoDatabase.getDataBase(this, applicationScope) }
    val repository by lazy { ObjectInfoRepository(objectDatabase!!.objectInfoDao()) }

}