package com.example.myapplication_task3.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication_task3.R
import com.example.myapplication_task3.models.User
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var toolbarMapActivity :Toolbar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var updateLocationButton: Button
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore
    private lateinit var addressText: TextView
    private lateinit var dateTimeText: TextView
    private var lastKnownLocation: Location? = null
    private val dateTime = getCurrentDateTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
                 toolbarMapActivity=findViewById(R.id.toolbar_map_activity)
        " ".also { title=it }
        setupActionBar()


        dateTimeText = findViewById(R.id.dateTimeText)
        addressText = findViewById(R.id.addressText)
        updateLocationButton = findViewById(R.id.btn_update_location_map_activity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        firestore = FirebaseFirestore.getInstance()

        mapView.onResume()


        updateLocationButton.setOnClickListener { updateLocation() }
    }
    private fun setupActionBar(){
        setSupportActionBar(toolbarMapActivity)

        val actionBar= supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_backbutton_black)

        }
        toolbarMapActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // Callback function when the map is ready
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        lastKnownLocation?.let {
            showCurrentLocation(it.latitude, it.longitude)
            getAddressFromLocation(it.latitude, it.longitude)
        } ?: run {
            getLastKnownLocation()
        }
    }

    // Function to get the last known location
    private fun getLastKnownLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Store location in Firestore database
                        storeLocationInFirestore(location.latitude,
                            location.longitude,
                            addressText.text.toString(),
                            dateTime)
                        // Show current location on the map
                        showCurrentLocation(location.latitude, location.longitude)

                        // Retrieve address from location coordinates
                        getAddressFromLocation(location.latitude, location.longitude)
                        // Update the last known location
                        lastKnownLocation = location
                    } else {
                        // If last known location is not available, request current location
                        requestCurrentLocation()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to retrieve last known location
                    val errorMessage =
                        "Failed to retrieve last known location. Error: ${e.message}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            // If location permission is not granted, request permission from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

    }

    // request current location from the fused location provider
    private fun requestCurrentLocation() {

        // Create a location request with high accuracy
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Build location settings request to check if device settings are configured for location updates
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@addOnSuccessListener
            }
            // Request the current location from fused location provider
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    storeLocationInFirestore(location.latitude, location.longitude,addressText.text.toString(), dateTime)
                    showCurrentLocation(location.latitude, location.longitude)
                    getAddressFromLocation(location.latitude, location.longitude)
                    lastKnownLocation = location
                }
            }.addOnFailureListener { e ->
                val errorMessage = "Failed to retrieve current location. Error: ${e.message}"
                Toast.makeText(this@MapActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this@MapActivity,
                        LOCATION_SETTINGS_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(
                        this@MapActivity,
                        "Failed to request location updates. Error: ${sendEx.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@MapActivity,
                    "Failed to request location updates. Error: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    // Function called when an activity launched by startActivityForResult() exits
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                // Request the current location from fused location provider
                fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Store location in Firestore database
                        storeLocationInFirestore(location.latitude,
                            location.longitude,
                            addressText.text.toString(),
                            dateTime)
                        // Show current location on the map
                        showCurrentLocation(location.latitude, location.longitude)

                        // Retrieve address from location coordinates
                        getAddressFromLocation(location.latitude, location.longitude)

                        // Update the last known location
                        lastKnownLocation = location
                    }
                }.addOnFailureListener { e ->
                    val errorMessage = "Failed to retrieve current location. Error: ${e.message}"
                    Toast.makeText(this@MapActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(
                    this,
                    "Location settings are not enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

   // update the location when the "Update Location" button is clicked
    private fun updateLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Show current location on the map
                        showCurrentLocation(location.latitude, location.longitude)
                        // Retrieve address from location coordinates
                        getAddressFromLocation(location.latitude, location.longitude)
                        // Update the last known location
                        lastKnownLocation = location
                       // Update date and time text
                        dateTimeText.text = dateTime
                        // Store location in Firestore database
                        storeLocationInFirestore(
                            location.latitude,
                            location.longitude,
                            addressText.text.toString(),
                            dateTime
                        )
                        // Show success dialog
                        showLocationUpdateDialog(true)
                    } else {
                        // Request the current location if last known location is not available
                        requestCurrentLocation()
                    }
                }
                .addOnFailureListener { e ->
                    val errorMessage =
                        "Failed to retrieve last known location. Error: ${e.message}"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            // Request location permission if not granted

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showLocationUpdateDialog(b: Boolean) {


    }




    // Function to get the current date and time
    private fun getCurrentDateTime(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(currentDate)
        val time = timeFormat.format(currentDate)
        return "$date $time"
    }

    // Function called when the user responds to the location permission request
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Update location if permission is granted
            updateLocation()
        }
    }

    //store the location in the Firestore database
    private fun storeLocationInFirestore(latitude: Double, longitude: Double, address: String,dateTime:String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        userId?.let {
            // Get the user document reference from Firestore
            val userRef = firestore.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the user object from the document snapshot
                        val user = documentSnapshot.toObject(User::class.java)
                        user?.let {
                            val updatedUser = User(
                                user.id,
                                user.name,
                                user.email,
                                user.reenterPassword,
                                latitude,
                                longitude,
                                address,
                                dateTime
                            )
                            userRef.set(updatedUser)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener { e ->
                                    val errorMessage = "Failed to store location. Error: ${e.message}"
                                    Toast.makeText(
                                        this@MapActivity,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    val errorMessage = "Failed to retrieve user data. Error: ${e.message}"
                    Toast.makeText(
                        this@MapActivity,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    //show the current location on the map
    private fun showCurrentLocation(latitude: Double, longitude: Double) {
        googleMap.clear()

        val markerOptions = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title("Current Location")
        googleMap.addMarker(markerOptions)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(latitude, longitude),
            15f
        )
        googleMap.moveCamera(cameraUpdate)
    }

    //retrieve the address from location coordinates
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val addressString = address.getAddressLine(0)
                addressText.setText(addressString) // Update to use the class member variable
            storeLocationInFirestore(latitude, longitude, addressText.text.toString(), dateTime)
        } else {
            val addressString = "Address not found" // Update to use the class member variable
                addressText.setText(addressString)
            storeLocationInFirestore(latitude, longitude, addressText.text.toString(), dateTime)
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val LOCATION_SETTINGS_REQUEST_CODE = 456

    }
}