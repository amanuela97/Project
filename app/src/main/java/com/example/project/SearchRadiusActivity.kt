package com.example.project

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.project.utility.Constants
import com.example.project.utility.GlobalObject
import com.google.android.gms.maps.MapFragment
import kotlinx.android.synthetic.main.activity_search_radius.*

class SearchRadiusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_radius)

        setUpTextInput()
        setUpSaveButton()
    }

    private fun setUpTextInput() {
        setUpDefaultValueOfTextInput()
        setUpTextChangedListener()
    }

    private fun setUpDefaultValueOfTextInput() {
        search_radius_input.setText((GlobalObject.SEARCH_RADIUS / 1000).toString())
    }

    private fun setUpTextChangedListener() {
        search_radius_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                search_radius_input.removeTextChangedListener(this)
                p0?.toString()?.toInt()?.let {
                    val maxSearchRadius = resources.getInteger(R.integer.max_search_radius) / 1000
                    if (it > maxSearchRadius) {
                        search_radius_input.setText(maxSearchRadius.toString())
                    }
                }
                search_radius_input.addTextChangedListener(this)
            }

        })
    }

    private fun setUpSaveButton() {
        button.setOnClickListener {
            val searchRadius = search_radius_input.text.toString().toInt() * 1000
            GlobalObject.SEARCH_RADIUS = searchRadius
            val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: null
            sharedPreferences?.let {
                with(it.edit()) {
                    putInt(Constants.SEARCH_RADIUS_KEY, searchRadius)
                    commit()
                    println(it.all)
                }
            }

            finish()
        }
    }
}