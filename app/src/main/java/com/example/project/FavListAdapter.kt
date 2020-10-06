package com.example.project

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.project.room_data.Restaurant
import com.example.project.room_data.RestaurantModel
import com.example.project.utility.Constants
import kotlinx.android.synthetic.main.fav_rv_custom_row.view.*


class FavListAdapter(context: Context, restaurantModel: RestaurantModel) :
    RecyclerView.Adapter<FavListAdapter.ListViewHolder>(){


    private var items =  emptyList<Restaurant>()

    // variable to hold context
    private val context: Context? = context
    private val model = restaurantModel
    class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fav_rv_custom_row, parent, false)
    )

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentItem = items[position]
        var bitmap: Bitmap? = null
        if(currentItem.photo != null){
            bitmap = BitmapFactory.decodeByteArray(currentItem.photo, 0, currentItem.photo.size)
        }
        holder.itemView.restaurant_img.setImageBitmap(bitmap)
        holder.itemView.restaurant_name.text = currentItem.name
        holder.itemView.vicinity.text = "${context?.getString(R.string.address)} ${currentItem.address}"
        val data: ArrayList<String> = arrayListOf()
        currentItem.business_status?.forEach { open_hours ->
            data.add(open_hours)
        }
        if (context != null) {
            ArrayAdapter(context, android.R.layout.simple_spinner_item, data).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                holder.itemView.weekday_text.adapter = adapter
            }
        }
        holder.itemView.phone_number.text = "${context?.getString(R.string.phone)} ${currentItem.phoneNumber ?: "-"}"
        holder.itemView.restaurant_rating.text = "${context?.getString(R.string.rating)} ${currentItem.rating ?: "-"}"


        //delete
        holder.itemView.delete_icon.setOnClickListener {
            Log.i(Constants.TAG,"Clicked")
            removeItem(position)
            val deletedRestaurant = Restaurant( currentItem.id,
                currentItem.name,
                currentItem.business_status,
                currentItem.photo,
                currentItem.open,
                currentItem.rating,
                currentItem.phoneNumber,
                currentItem.address,
                currentItem.location)
            removeRestaurant(deletedRestaurant)
        }
    }


    private fun removeRestaurant(deletedRestaurant: Restaurant) {
        val dialog = AlertDialog.Builder(context)
            .setMessage(R.string.alert_delete)
            .setPositiveButton("GO BACK") { _, _ ->
            }
            .setNegativeButton("DISCARD") { _, _ ->
                model.deleteFavRestaurant(deletedRestaurant)
                notifyDataSetChanged()
                Toast.makeText(context, context?.getText(R.string.delete), Toast.LENGTH_SHORT)
                    .show()
            }.create()
            dialog.setCancelable(false)
            dialog.show()
    }

    private fun removeItem(position: Int) {
        setData(items.drop(position))
        notifyItemRemoved(position)
    }


    fun setData(restaurants: List<Restaurant>?){
        if(restaurants != null){
            Log.i(Constants.TAG, "$restaurants")
            this.items = restaurants
            notifyDataSetChanged()
        }
    }


}