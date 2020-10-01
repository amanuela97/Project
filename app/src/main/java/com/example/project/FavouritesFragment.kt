package com.example.project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.room_data.RestaurantModel
import kotlinx.android.synthetic.main.fragment_favourites.view.*


class FavouritesFragment : Fragment() {

    private lateinit var restaurantModel: RestaurantModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view =  inflater.inflate(R.layout.fragment_favourites, container, false)

        //setup recyclerview adapter
        val adapter = FavListAdapter()
        val rV = view.fav_rv
        rV.adapter = adapter
        rV.layoutManager = LinearLayoutManager(requireContext())

        // model for the data
        restaurantModel = ViewModelProviders.of(this).get(RestaurantModel::class.java)

        restaurantModel.favouriteRestaurants.observe(viewLifecycleOwner,{favRestaurants ->
                if(!favRestaurants.isNullOrEmpty()){
                    adapter.setData(favRestaurants)
                }
        })

        return view
    }

}