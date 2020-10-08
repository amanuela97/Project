package com.example.project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.room_data.RestaurantModel
import kotlinx.android.synthetic.main.activity_favourites.*

class FavouritesActivity : AppCompatActivity() {

    private lateinit var restaurantModel: RestaurantModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        // model for the data
        restaurantModel = ViewModelProviders.of(this).get(RestaurantModel::class.java)

        //setup recyclerview adapter
        val adapter = FavListAdapter(this, restaurantModel)
        val rV = fav_rv
        rV.adapter = adapter
        rV.layoutManager = LinearLayoutManager(this)


        restaurantModel.favouriteRestaurants.observe(this, {favRests ->
            //when there is no favorite then show this layout
                if (favRests.isNullOrEmpty()){
                    fav_list_empty.visibility = View.VISIBLE
                    empty_list_text.visibility = View.VISIBLE
                }
                adapter.setData(favRests)
        })

    }
}