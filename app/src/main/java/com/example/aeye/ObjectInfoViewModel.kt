package com.example.aeye

import androidx.lifecycle.*
import com.example.aeye.database.ObjectInfo
import com.example.aeye.database.ObjectInfoRepository
import kotlinx.coroutines.launch

class ObjectInfoViewModel(private val repository: ObjectInfoRepository) : ViewModel() {

    private val objectInfoData : LiveData<List<ObjectInfo>> = repository.allObjects.asLiveData()

    fun insert (newInfo: ObjectInfo) = viewModelScope.launch { repository.insert(newInfo) }

    fun delete (objectInfo: ObjectInfo) = viewModelScope.launch { repository.delete(objectInfo) }

    fun findByClassName (className : String) = viewModelScope.launch { repository.findByClassName(className) }
}

class ObjectInfoViewModelFactory(private val repository: ObjectInfoRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObjectInfoViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ObjectInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}