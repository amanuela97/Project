package com.example.project

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.NearbySearch
import com.example.project.repository.Repository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository): ViewModel() {

    val nearbySearchresponse: MutableLiveData<NearbySearch> = MutableLiveData()

    fun getNearbySearch(location: String, radius: String, types: String, key: String){
        viewModelScope.launch {
            val response = repository.getNearbySearch(location, radius, types, key)
            nearbySearchresponse.value = response
        }
    }

}