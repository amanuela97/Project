package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.example.project.utility.GlobalObject
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
                if (p0?.toString() == "") return
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
            val stringValueOfInput = search_radius_input.text.toString()
            if (stringValueOfInput != "") {
                val searchRadius = search_radius_input.text.toString().toInt() * 1000
                GlobalObject.SEARCH_RADIUS = searchRadius
                val sharedPreferences = getSharedPreferences(getString(R.string.app_settings_key), Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt(getString(R.string.search_radius_key), searchRadius)
                    commit()
                }
            }

            finish()
        }
    }
}