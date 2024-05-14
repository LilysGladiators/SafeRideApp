package com.example.saferide

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var database: DatabaseReference
    private var rideId: String? = null
    private lateinit var pickupLocationTextView: TextView
    private lateinit var destinationTextView: TextView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var carMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    private val locationUpdateInterval = 5000L // Update interval in milliseconds
    private val locationUpdateDistance = 10f // Update distance in meters

    private var dummyPickupPoint: LatLng? = null
    private var dummyDestinationPoint: LatLng? = null
    private var dummyCarLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        pickupLocationTextView = findViewById(R.id.pickupLocationTextView)
        destinationTextView = findViewById(R.id.destinationTextView)

        database = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set dummy data for the car's location and the pickup and destination points
        dummyPickupPoint = LatLng(37.7749, -122.4194) // Example pickup point
        dummyDestinationPoint = LatLng(37.7833, -122.4167) // Example destination point
        dummyCarLocation = LatLng(37.7749, -122.4194) // Example car location

        // Get the rideId from the Intent extras
        rideId = intent.getStringExtra("rideId")

        // Get the required information from the Intent extras
        val pickupLatitude = intent.getDoubleExtra("pickupLatitude", 0.0)
        val pickupLongitude = intent.getDoubleExtra("pickupLongitude", 0.0)
        val destinationLatitude = intent.getDoubleExtra("destinationLatitude", 0.0)
        val destinationLongitude = intent.getDoubleExtra("destinationLongitude", 0.0)

        // Update the UI with the received information
        updateUI(
            pickupLatitude,
            pickupLongitude,
            destinationLatitude,
            destinationLongitude
        )

        // Fetch the ride request details from the database
        fetchRideRequestDetails()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onMapReady(googleMapInstance: GoogleMap) {
        googleMap = googleMapInstance

        // Enable the zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Set the map type (e.g., normal, satellite, terrain)
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Get the user's current location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Add a marker at the user's current location
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                    )
                }
            }
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Display the dummy route and car location on the map
        displayDummyRouteOnMap()
        updateDummyCarLocation()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                locationUpdateInterval,
                locationUpdateDistance,
                this
            )
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        // Update the car's location
        val carLocation = LatLng(location.latitude, location.longitude)
        updateCarLocation(carLocation)
    }

    private fun fetchRideRequestDetails() {
        if (rideId != null) {
            database.child("rideRequests").child(rideId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rideRequest = snapshot.getValue(RideRequest::class.java) ?: return

                    val pickupLatitude = rideRequest.pickupLatitude ?: 0.0
                    val pickupLongitude = rideRequest.pickupLongitude ?: 0.0
                    val destinationLatitude = rideRequest.destinationLatitude ?: 0.0
                    val destinationLongitude = rideRequest.destinationLongitude ?: 0.0

                    updateUI(
                        pickupLatitude,
                        pickupLongitude,
                        destinationLatitude,
                        destinationLongitude
                    )

                    displayRouteOnMap(
                        LatLng(pickupLatitude, pickupLongitude),
                        LatLng(destinationLatitude, destinationLongitude)
                    )

                    // Clear the dummy route and car marker
                    googleMap.clear()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    private fun updateUI(
        pickupLatitude: Double,
        pickupLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ) {
        // Update UI elements with ride request details
        pickupLocationTextView.text = getString(
            R.string.pickup_location_format,
            pickupLatitude,
            pickupLongitude
        )
        destinationTextView.text = getString(
            R.string.destination_format,
            destinationLatitude,
            destinationLongitude
        )
    }

    private fun displayRouteOnMap(pickupPoint: LatLng, destinationPoint: LatLng) {
        // Create a PolylineOptions object for the route
        val polylineOptions = PolylineOptions()
            .add(pickupPoint)
            .add(destinationPoint)
            .color(Color.BLUE)
            .width(5f)
            .pattern(listOf(Dash(20f), Gap(10f)))

        // Create a Polyline object and add it to the map
        googleMap.addPolyline(polylineOptions)

        // Move the camera to focus on the route
        val bounds = LatLngBounds.Builder()
            .include(pickupPoint)
            .include(destinationPoint)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    private fun displayDummyRouteOnMap() {
        if (dummyPickupPoint != null && dummyDestinationPoint != null) {
            // Create a PolylineOptions object for the dummy route
            val polylineOptions = PolylineOptions()
                .add(dummyPickupPoint!!)
                .add(dummyDestinationPoint!!)
                .color(Color.BLUE)
                .width(5f)
                .pattern(listOf(Dash(20f), Gap(10f)))

            // Create a Polyline object and add it to the map
            googleMap.addPolyline(polylineOptions)

            // Move the camera to focus on the dummy route
            val bounds = LatLngBounds.Builder()
                .include(dummyPickupPoint!!)
                .include(dummyDestinationPoint!!)
                .build()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    private fun updateDummyCarLocation() {
        if (dummyCarLocation != null) {
            // Update car marker on the map
            if (carMarker == null) {
                carMarker = googleMap.addMarker(
                    MarkerOptions()
                        .position(dummyCarLocation!!)
                        .title("Car")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon))
                )
            } else {
                carMarker?.position = dummyCarLocation!!
            }
        }
    }

    private fun updateCarLocation(carLocation: LatLng) {
        // Update car marker on the map
        if (carMarker == null) {
            carMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(carLocation)
                    .title("Car")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon))
            )
        } else {
            carMarker?.position = carLocation
        }
    }
}