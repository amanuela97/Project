package com.example.project.room_data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RestaurantModel(application: Application): AndroidViewModel(application) {

    val favouriteRestaurants: LiveData<List<Restaurant>> = RestaurantDB.get(application).restaurantDao().getAllRestaurants()
    private val ref = RestaurantDB.get(application)


    // suspended so call with coroutine
  fun addFavRestaurant(restaurant: Restaurant){
        GlobalScope.launch(Dispatchers.IO){
            ref.restaurantDao().insertRestaurant(restaurant)
        }
    }

    fun updateFavRestaurant(restaurant: Restaurant){
        GlobalScope.launch(Dispatchers.IO) {
            ref.restaurantDao().updateRestaurant(restaurant)
        }
    }

     fun deleteFavRestaurant(restaurant: Restaurant){
        GlobalScope.launch(Dispatchers.IO){
            ref.restaurantDao().deleteRestaurant(restaurant)
        }
    }


}