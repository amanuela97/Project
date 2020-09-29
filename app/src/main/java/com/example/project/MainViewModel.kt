package com.example.project

import android.provider.SyncStateContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.FindPlaceFromTextResult
import com.example.project.model.NearbySearchResult
import com.example.project.repository.Repository
import com.example.project.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository): ViewModel() {

    val nearbySearchresponse: MutableLiveData<NearbySearchResult> = MutableLiveData()
    val findPlaceFromTextResponse: MutableLiveData<FindPlaceFromTextResult> = MutableLiveData()

    fun getNearbySearch(location: String, radius: String, types: String, key: String){
        viewModelScope.launch {
            val response = repository.getNearbySearch(location, radius, types, key)
            nearbySearchresponse.value = response
        }
    }

    fun findPlaceFromTextSearch(input: String, inputtype: String, fields: String, key: String){
        viewModelScope.launch {
            val response = repository.findPlaceFromText(input, inputtype, fields, key)
            if (response.status != "ZERO_RESULTS"){
                findPlaceFromTextResponse.value = response
            }else{
                findPlaceFromTextResponse.value = null
            }
        }
    }

}