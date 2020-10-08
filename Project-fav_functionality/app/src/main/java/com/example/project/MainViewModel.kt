package com.example.project

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.FindPlaceFromTextResult
import com.example.project.model.NearbySearch
import com.example.project.model.PlaceDetailsResult
import com.example.project.model.WeatherResult
import com.example.project.repository.Repository
import com.example.project.utility.Constants
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository): ViewModel() {

    val nearbySearchResponse: MutableLiveData<NearbySearch> = MutableLiveData()
    val findPlaceFromTextResponse: MutableLiveData<FindPlaceFromTextResult> = MutableLiveData()
    val placeDetailsResponse: MutableLiveData<PlaceDetailsResult> = MutableLiveData()
    val placeDetailsResponse2: MutableLiveData<PlaceDetailsResult> = MutableLiveData()
    val weatherResult: MutableLiveData<WeatherResult> = MutableLiveData()

    fun getNearbySearch(location: String, radius: String, types: String, key: String){
        viewModelScope.launch {
            val response = repository.getNearbySearch(location, radius, types, key)
            nearbySearchResponse.value = response
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

    fun getPlaceDetails(place_id: String, fields: String, key: String){
        viewModelScope.launch {
            val response = repository.getPlaceDetails(place_id,fields,key)
            placeDetailsResponse.value = response.result
        }
    }

    fun getPlaceDetails2(place_id: String, fields: String, key: String){
        viewModelScope.launch {
            val response = repository.getPlaceDetails(place_id,fields,key)
            placeDetailsResponse2.value = response.result
        }
    }

    fun getWeather(lat: Double, lon: Double, units: String?, appid: String?){
        viewModelScope.launch {
            val response = repository.getWeather(lat,lon,units,appid)
            Log.i(Constants.TAG,"$response" )
            weatherResult.value = response
        }
    }

}