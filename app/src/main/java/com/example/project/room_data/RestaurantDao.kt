package com.example.project.room_data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RestaurantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant)

    @Query("SELECT * FROM restaurant")
    fun getAllRestaurants(): LiveData<List<Restaurant>>

    @Update
    fun updateRestaurant(restaurant: Restaurant)

    @Delete
    fun deleteRestaurant(restaurant: Restaurant)

}