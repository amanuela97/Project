package com.example.project

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.project.repository.Repository
import com.example.project.utility.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_weather.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity(), SensorEventListener {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: MainViewModel

    private lateinit var sensorManager: SensorManager
    private var humidity: Sensor? = null
    private var isHumiditySensorAvail: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null){
            humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
            isHumiditySensorAvail = true
        }else{
            Log.i(Constants.TAG, "Humidity sensor unavailable")
            isHumiditySensorAvail = false
        }

        // setting up view model
        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
        if (Constants.Location != null){
            val latitude = Constants.Location?.latitude
            val longitude = Constants.Location?.longitude
            if (latitude != null) {
                if (longitude != null) {
                    viewModel.getWeather(latitude.toDouble(), longitude.toDouble(),Constants.UNIT,Constants.OPEN_WEATHER_API)
                }
            }
            viewModel.weatherResult.observe(this, { response ->
                Log.i(Constants.TAG,"$response - response weather" )
                val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return@observe
                with (sharedPref.edit()) {
                    putFloat(getString(R.string.temp), response.main.temp)
                    putInt(getString(R.string.pressure),response.main.pressure)
                    putInt(getString(R.string.date),response.dt)
                    putString(getString(R.string.icon),response.weather[0].icon)
                    putString(getString(R.string.name), response.name)
                    putString(getString(R.string.description), response.weather[0].description)
                    putString(getString(R.string.country), response.sys.country)
                    if (!isHumiditySensorAvail){
                        putInt(getString(R.string.humidity),response.main.humidity)
                    }
                    apply()
                }
            })
        }

        setUpView()

    }

    @SuppressLint("SetTextI18n")
    private fun setUpView(){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val cityName = sharedPref.getString(getString(R.string.name), "-")
        val country = sharedPref.getString(getString(R.string.country), "-")
        val temp = sharedPref.getFloat(getString(R.string.temp), 0.toFloat())
        val description = sharedPref.getString(getString(R.string.description), "-")
        val pressure = sharedPref.getInt(getString(R.string.pressure), 0)
        val date = sharedPref.getInt(getString(R.string.date), 0)
        var humidity: Int? = null
        if (!isHumiditySensorAvail){
            humidity = sharedPref.getInt(getString(R.string.humidity), 222)
        }
        val dateString = getDateTime(date.toString())
        when(sharedPref.getString(getString(R.string.icon), "")){
            "01d" -> weather_icon.setImageResource(R.drawable.sunny)
            "02d" -> weather_icon.setImageResource(R.drawable.cloud)
            "03d" -> weather_icon.setImageResource(R.drawable.cloud)
            "04d" -> weather_icon.setImageResource(R.drawable.cloud)
            "04n" -> weather_icon.setImageResource(R.drawable.cloud)
            "10d" -> weather_icon.setImageResource(R.drawable.rain)
            "11d" -> weather_icon.setImageResource(R.drawable.storm)
            "13d" -> weather_icon.setImageResource(R.drawable.snowflake)
            "01n" -> weather_icon.setImageResource(R.drawable.cloud)
            "02n" -> weather_icon.setImageResource(R.drawable.cloud)
            "03n" -> weather_icon.setImageResource(R.drawable.cloud)
            "10n" -> weather_icon.setImageResource(R.drawable.cloud)
            "11n" -> weather_icon.setImageResource(R.drawable.rain)
            "13n" -> weather_icon.setImageResource(R.drawable.snowflake)
        }

        city_field.text = "$cityName, $country"
        temp_field.text = "$temp ℃"
        description_field.text = description
        pressure_field.text = "${getString(R.string.pressure)} $pressure hPa"
        weather_date.text = dateString
        if (humidity != null){
            humidity_field.text = "${getString(R.string.humidity)} $humidity%"
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val netDate = Date(s.toLong() * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null && isHumiditySensorAvail) {
            humidity_field.text = p0.values[0].toString() + "%"
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onResume() {
        super.onResume()
        // Register a listener for the sensor.
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        //unregister the sensor when the activity pauses.
        sensorManager.unregisterListener(this)
    }
}