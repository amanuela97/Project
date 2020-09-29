package com.example.project.room_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Restaurant::class], version = 1, exportSchema = false)
abstract class RestaurantDB: RoomDatabase(){

    abstract fun restaurantDao(): RestaurantDao

    companion object{
        private var INSTANCE: RestaurantDB? = null

        @Synchronized // protects from execution from multiple concurrent threads
        fun get(context: Context): RestaurantDB{
            if (INSTANCE == null){
                INSTANCE =
                    Room.databaseBuilder(context.applicationContext,
                        RestaurantDB::class.java, "RestaurantDB.db").build()
            }
            return INSTANCE!!
        }
    }
}