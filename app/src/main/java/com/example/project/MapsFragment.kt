package com.example.project

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    //A default location (Sydney, Australia)
    private val defaultLocation = LatLng(-34.0, 151.0)


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false

    //The geographical location where the device is currently located.
    private var lastKnownLocation: Location? = null

    companion object{
        private const val TAG = "GGM"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DEFAULT_ZOOM = 15
        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_maps, container, false)
        // Retrieve location and camera position from saved instance state. And add marker
        lastKnownLocation = savedInstanceState?.getParcelable(KEY_LOCATION)
        cameraPosition = savedInstanceState?.getParcelable(KEY_CAMERA_POSITION)
        return v

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    //called when map is ready for use
    override fun onMapReady(map: GoogleMap?) {

        this.map = map

        //ask for location permission
        getLocationPermission()

        // if location is not turned on direct user to settings
        if(checkIfLocationIsOn() && locationPermissionGranted){
            activity?.supportFragmentManager?.let { LocationDialogFragment().show(it, "TAG") }
        }

        // Turn on locations settings on map
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    //Saves the state of the map when the activity is paused.
    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map?.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    // Change the maps UI settings based on the permission result.
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //get recent location of the device
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val cLocation = LatLng(lastKnownLocation!!.latitude,lastKnownLocation!!.longitude)
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                cLocation, DEFAULT_ZOOM.toFloat()))
                            map?.addMarker(MarkerOptions().position(cLocation).title(getAddress(cLocation.latitude, cLocation.longitude)))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults location.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.addMarker(MarkerOptions().position(defaultLocation).title(getAddress(defaultLocation.latitude,defaultLocation.longitude)))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    // get address name using lat and lng
    private fun getAddress(lat: Double?, lng: Double?): String {
        val geoCoder = Geocoder(requireActivity())
        val list = geoCoder.getFromLocation(lat?:0.0, lng?:0.0, 1)
        return list[0].getAddressLine(0)
    }

     //Prompts the user for permission to use the device location.
    private fun getLocationPermission() {
         // Request location permission
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun checkIfLocationIsOn(): Boolean{
        val lm = activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return (!gpsEnabled && !networkEnabled)

    }

    //called after user response to permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when(requestCode){
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true

                }
            }
        }
    }

    //dialogue to remind user to turn on  location
    class LocationDialogFragment : DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                //  dialog construction
                val builder = AlertDialog.Builder(it)
                builder.setMessage(R.string.alert_message)
                    .setPositiveButton("OK") { _, _ ->
                        // request location action
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("CANCEL") { _, _ ->
                        Toast.makeText(context, getText(R.string.canceled), Toast.LENGTH_SHORT).show()
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }


}