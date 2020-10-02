package com.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.project.model.NearbySearchResult
import com.example.project.model.PlaceDetailsResult
import com.example.project.repository.Repository
import com.example.project.room_data.RestaurantModel
import com.example.project.utility.Constants
import com.example.project.utility.GlobalObject
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import java.io.IOException


class MapsFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var predictionsList: List<AutocompletePrediction>
    private var suggestedPlaces: ArrayList<Place> = ArrayList()
    private var searchedPlace: PlaceDetailsResult? = null
    private var searchedPlaceMarker: Marker? = null

    //A default location (Sydney, Australia)
    private val defaultLocation = LatLng(-34.0, 151.0)


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false

    //The geographical location where the device is currently located.
    private var lastKnownLocation: Location? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var restaurantModel: RestaurantModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_maps, container, false)
        //  get nearby places by setting up retrofit and view model
        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        //remove action bar as well
//        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        // Retrieve location and camera position from saved instance state. And add marker
        lastKnownLocation = savedInstanceState?.getParcelable(Constants.KEY_LOCATION)
        cameraPosition = savedInstanceState?.getParcelable(Constants.KEY_CAMERA_POSITION)

        Places.initialize(requireActivity(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireActivity())
        return v

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )

        //initialize database to store fav restaurant info
        restaurantModel = ViewModelProvider(this).get(RestaurantModel::class.java)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    //called when map is ready for use
    override fun onMapReady(map: GoogleMap?) {

        this.map = map

        //ask for location permission
        getLocationPermission()

        // if location is not turned on direct user to settings
        directUserToLocationSettings()

        // Turn on locations settings on map
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        handleRestaurantSearch()

    }

    private fun handleRestaurantSearch() {
        searchBar.addTextChangeListener(object : TextWatcher {
            val token = AutocompleteSessionToken.newInstance()
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //show a list of suggestions or autocomplete
                val predictionsRequest = FindAutocompletePredictionsRequest.builder()
                    .setCountry("fi")
                    .setOrigin(lastKnownLocation?.latitude?.let {
                        lastKnownLocation?.longitude?.let { it1 ->
                            LatLng(
                                it,
                                it1
                            )
                        }
                    })
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(p0.toString())
                    .build()
                placesClient.findAutocompletePredictions(predictionsRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val predictionResponse = task.result
                            if (predictionResponse != null) {
                                predictionsList = predictionResponse.autocompletePredictions
                                val autoCompleteResults = ArrayList<String>()
                                predictionsList.forEach {
                                    autoCompleteResults.add(it.getFullText(null).toString())
                                }
                                searchBar.updateLastSuggestions(autoCompleteResults)
                            }
                        } else {
                            Log.d(Constants.TAG, "fetching predictions failed")
                        }
                    }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })


        searchBar.setSuggestionsClickListener(object : SuggestionsAdapter.OnItemViewClickListener {
            override fun OnItemClickListener(position: Int, v: View?) {
                if (position >= predictionsList.size) {
                    return
                }
                val selectedSuggestion = predictionsList[position]
                val suggestion = searchBar.lastSuggestions[position].toString()
                searchBar.text = suggestion
                searchBar.clearSuggestions()

                // close keyboard
                closeKeyboard()

                //use id to get location
                val placeId = selectedSuggestion.placeId
                val placeFields = listOf(
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS,
                    Place.Field.OPENING_HOURS,
                    Place.Field.RATING,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.NAME
                )
                val fetchPlaceReq = FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient.fetchPlace(fetchPlaceReq).addOnSuccessListener { response ->
                    if (response.place.latLng != null) {
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                response.place.latLng, Constants.DEFAULT_ZOOM.toFloat()
                            )
                        )
                        searchedPlaceMarker = map?.addMarker(
                            MarkerOptions().position(response.place.latLng!!)
                                .title(response.place.name)
                        )
                        suggestedPlaces.add(response.place)
                    }
                }.addOnFailureListener { ex ->
                    ex.printStackTrace()
                    Log.i(Constants.TAG, "Error fetching place data")
                }


            }

            override fun OnItemDeleteListener(position: Int, v: View?) {
            }

        })

        // search for place when user presses enter
        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                val searchState = if (enabled) "enabled" else "disabled"
                Log.i(Constants.TAG, "Search $searchState")
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                searchBar.clearSuggestions()
                if (text?.isNotEmpty()!!) {
                    startSearch(text.toString())
                }
            }

            override fun onButtonClicked(buttonCode: Int) {

                when (buttonCode) {
                    MaterialSearchBar.BUTTON_BACK -> {
                        searchBar.clearSuggestions()
                        searchBar.closeSearch()
                        searchedPlaceMarker?.remove()
                    }
                    MaterialSearchBar.BUTTON_NAVIGATION -> {
                        (activity as MainActivity).toggleDrawer()
                    }
                }
            }

        })

        // remove marker when x is clicked
        searchBar.findViewById<View>(R.id.mt_clear).setOnClickListener {
            searchBar.closeSearch()
            searchBar.clearSuggestions()
            Log.i(Constants.TAG, "${searchedPlaceMarker?.title}")
            searchedPlaceMarker?.remove()
            searchedPlace = null
        }
    }

    private fun startSearch(text: String) {
        viewModel.findPlaceFromTextSearch(
            text,
            Constants.INPUT_TYPE,
            Constants.FIELDS,
            getString(R.string.google_maps_key)
        )
        viewModel.findPlaceFromTextResponse.observe(this, { place ->
            if (place != null) {
                val response = place.candidates[0]
                viewModel.getPlaceDetails2(response.place_id,Constants.FIELDS_2,getString(R.string.google_maps_key))
                viewModel.placeDetailsResponse2.observe(this,{ placeWithDetails2 ->
                    val latLng = LatLng(placeWithDetails2.geometry.location.lat, placeWithDetails2.geometry.location.lng)
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            latLng, Constants.DEFAULT_ZOOM.toFloat()
                        )
                    )
                    searchedPlaceMarker = map?.addMarker(
                        MarkerOptions().position(latLng)
                            .title(placeWithDetails2.name)
                    )
                    searchedPlace = placeWithDetails2
                })
            } else {
                Toast.makeText(requireContext(), R.string.zero_results, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun closeKeyboard() {
        //close keyboard
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun directUserToLocationSettings() {
        if (checkIfLocationIsOn() && locationPermissionGranted) {
            activity?.supportFragmentManager?.let { LocationDialogFragment().show(it, "TAG") }
        }
    }

    //Saves the state of the map when the activity is paused.
    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(Constants.KEY_CAMERA_POSITION, map?.cameraPosition)
            outState.putParcelable(Constants.KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    // Change the maps UI settings based on the permission result.
    @SuppressLint("ResourceType")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            //add padding so my location Button is not hidden under search bar
            map?.setPadding(0, 300, 0, 0)
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
                //set up user location marker icon
                val bitM = BitmapFactory.decodeResource(resources, R.drawable.placeholder)
                val bitResized = Bitmap.createScaledBitmap(bitM, 120, 120, false)

                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && !checkIfLocationIsOn()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val cLocation = LatLng(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                            )

                            // get nearby places/restaurants
                            val position =
                                "${lastKnownLocation!!.latitude},${lastKnownLocation!!.longitude}"
                            viewModel.getNearbySearch(
                                position, "${GlobalObject.SEARCH_RADIUS}", Constants.TYPE, getString(
                                    R.string.google_maps_key
                                )
                            )
                            viewModel.nearbySearchResponse.observe(this, { response ->
                                Log.d(Constants.TAG, "${response.results}")
                                markNearbyPlacesOnMap(response.results)
                            })

                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    cLocation, Constants.DEFAULT_ZOOM.toFloat()
                                )
                            )

                            BitmapDescriptorFactory.fromResource(R.drawable.house)
                            map?.addMarker(
                                MarkerOptions().position(cLocation).title(
                                    getAddress(
                                        cLocation.latitude,
                                        cLocation.longitude
                                    )
                                ).icon(BitmapDescriptorFactory.fromBitmap(bitResized))
                            )
                        }
                    } else {
                        Log.d(Constants.TAG, "Current location is null. Using defaults location.")
                        Log.e(Constants.TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, Constants.DEFAULT_ZOOM.toFloat())
                        )
                        map?.addMarker(
                            MarkerOptions().position(defaultLocation).title(
                                getAddress(
                                    defaultLocation.latitude,
                                    defaultLocation.longitude
                                )
                            ).icon(BitmapDescriptorFactory.fromBitmap(bitResized))
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun markNearbyPlacesOnMap(places: List<NearbySearchResult>) {
        val bit = BitmapFactory.decodeResource(resources, R.drawable.restaurant)
        val bitResized = Bitmap.createScaledBitmap(bit, 120, 120, false)
        places.forEach {
            map?.addMarker(
                MarkerOptions().position(
                    LatLng(it.geometry.location.lat, it.geometry.location.lng)
                ).title(
                    it.name
                ).icon(BitmapDescriptorFactory.fromBitmap(bitResized))
            )
        }
        close_card.setOnClickListener { card.visibility = View.INVISIBLE }
        map?.setOnMarkerClickListener { marker ->
            closeKeyboard()
            if (suggestedPlaces.isNotEmpty()) {
                suggestedPlaces.forEach {place ->
                    if (place.name == marker.title && marker.title != getAddress(
                            lastKnownLocation?.latitude,
                            lastKnownLocation?.longitude
                        )
                    ) {
                        //get photo of restaurant
                        place.photoMetadatas?.get(0)?.let { it1 ->
                            getRestaurantImageWithMetaData(
                                it1
                            )
                        }

                        setTextValues(place.name,place.address,place.rating,place.isOpen,place.phoneNumber,place.openingHours?.weekdayText)

                    }
                }
            }
            if (searchedPlace != null && marker.title == searchedPlace?.name) {
                setUpCard(
                    searchedPlace?.name,
                    searchedPlace?.formatted_address,
                    searchedPlace?.rating,
                    searchedPlace?.opening_hours?.open_now,
                    searchedPlace?.photos?.get(0)?.photo_reference,
                    searchedPlace?.formatted_phone_number,
                    searchedPlace?.opening_hours?.weekday_text

                )
            }
            places.forEach { place ->
                if (place.name == marker.title && marker.title != getAddress(
                        lastKnownLocation?.latitude,
                        lastKnownLocation?.longitude
                    )
                ) {
                    Log.i(Constants.TAG, "here")
                    viewModel.getPlaceDetails(place.place_id,Constants.FIELDS_2,getString(R.string.google_maps_key))
                    viewModel.placeDetailsResponse.observe(this,{placeWithDetail ->
                        setUpCard(
                            placeWithDetail.name,
                            placeWithDetail.formatted_address,
                            placeWithDetail.rating,
                            placeWithDetail.opening_hours?.open_now,
                            placeWithDetail.photos[0].photo_reference,
                            placeWithDetail.formatted_phone_number,
                            placeWithDetail.opening_hours?.weekday_text
                        )
                    })

                }
            }
            false
        }
    }

    private fun setUpCard(
        name: String?,
        address: String?,
        rating: Double?,
        openNow: Boolean?,
        photoRef: String?,
        phoneNumber: String?,
        businessHours: List<String>?
    ) {
        //get photo of restaurant
        GlobalScope.launch(Dispatchers.IO) {
            getRestaurantImage(photoRef)
        }
        setTextValues(name,address,rating,openNow,phoneNumber,businessHours)
    }

    @SuppressLint("SetTextI18n")
    private fun setTextValues(
        name: String?,
        address: String?,
        rating: Double?,
        openNow: Boolean?,
        phoneNumber: String?,
        businessHours: List<String>?
    ) {
        restaurant_name.text = name
        vicinity.text = "${getString(R.string.address)}  $address"
        if (openNow != null) {
            if (openNow) {
                restaurant_opening_hours.text = getText(R.string.opening_status)
                restaurant_opening_hours.setTextColor(Color.GREEN)
            } else {
                restaurant_opening_hours.text = getString(R.string.opening_status2)
                restaurant_opening_hours.setTextColor(Color.RED)
            }
            if (businessHours?.isNotEmpty()!!){
                val data: ArrayList<String> = arrayListOf()
                businessHours.forEach { open_hours ->
                    data.add(open_hours)
                }
                ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,data).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    weekday_text.adapter = adapter
                }
            }
        }
        phone_number.text = "${getString(R.string.phone)} ${phoneNumber ?: "-"}"
        restaurant_rating.text = "${getString(R.string.rating)} ${rating ?: "-"}"
        card.visibility = View.VISIBLE
    }

    private fun getRestaurantImage(photoRef: String?) {
        val client = OkHttpClient()
        val url =
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1100&photoreference=$photoRef&key=AIzaSyDW5ImALwzJx8h8RX9uFi6RDM7LiYc6UuI"
        val request = okhttp3.Request.Builder().url(url).build()
        Log.i(Constants.TAG, url)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i(Constants.TAG, "failed ok http")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val myResponse = response.body?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(myResponse)
                    Log.i(Constants.TAG, "$bitmap")
                    requireActivity().runOnUiThread {
                        restaurant_img.setImageBitmap(
                            Bitmap.createScaledBitmap(
                                bitmap,
                                600,
                                500,
                                true
                            )
                        )
                    }
                }
            }
        })
    }

    private fun getRestaurantImageWithMetaData(photoMetadata: PhotoMetadata) {
        // Create a FetchPhotoRequest.
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(1200)
            .build()
        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse: FetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                requireActivity().runOnUiThread {
                    restaurant_img.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 600, 500, true))
                }
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(Constants.TAG, "Place not found: " + exception.message)
                    val statusCode = exception.statusCode
                    Log.e(Constants.TAG, "Error status code: $statusCode")
                }
            }
    }


    // get address name using lat and lng
    private fun getAddress(lat: Double?, lng: Double?): String {
        val geoCoder = Geocoder(requireActivity())
        val list = geoCoder.getFromLocation(lat ?: 0.0, lng ?: 0.0, 1)
        return list[0].getAddressLine(0)
    }

    //Prompts the user for permission to use the device location.
    private fun getLocationPermission() {
        // Request location permission
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun checkIfLocationIsOn(): Boolean {
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
        when (requestCode) {
            Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true

                }
            }
        }
    }

    //dialogue to remind user to turn on  location
    class LocationDialogFragment : DialogFragment() {
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
                        Toast.makeText(context, getText(R.string.canceled), Toast.LENGTH_SHORT)
                            .show()
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}
