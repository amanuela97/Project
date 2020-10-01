package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.room_data.Restaurant

class FavListAdapter() :
    RecyclerView.Adapter<FavListAdapter.ListViewHolder>(){


    private var items =  emptyList<Restaurant>()
    class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fav_rv_custom_row,parent,false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: FavListAdapter.ListViewHolder, position: Int) {

    }


    fun setData(OwnerAndPet: List<Restaurant>?){
        if (OwnerAndPet != null) {
            this.items = OwnerAndPet
            notifyDataSetChanged()
        }
    }


}