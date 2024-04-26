package com.example.saferide

import android.Manifest
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var database: DatabaseReference
    private var rideId: String? = null
    private lateinit var pickupLocationTextView: TextView
    private lateinit var destinationTextView: TextView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var rideRequest: RideRequest

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

        // Get the rideId from the Intent extras
        rideId = intent.getStringExtra("rideId")

        // Fetch the ride request details from the database
        fetchRideRequestDetails()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    }

    private fun fetchRideRequestDetails() {
        if (rideId != null) {
            database.child("rideRequests").child(rideId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rideRequest = snapshot.getValue(RideRequest::class.java) ?: return
                    updateUI()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    private fun updateUI() {
        // Update UI elements with ride request details
        pickupLocationTextView.text = rideRequest.pickupLocation
        destinationTextView.text = rideRequest.destination

        // Display the pickup and destination locations on the map
        displayPickupAndDestinationOnMap(
            LatLng(rideRequest.pickupLatitude, rideRequest.pickupLongitude),
            LatLng(rideRequest.destinationLatitude, rideRequest.destinationLongitude)
        )
    }

    private fun displayPickupAndDestinationOnMap(pickupLatLng: LatLng, destinationLatLng: LatLng) {
        // Clear any existing markers
        googleMap.clear()

        // Add markers for pickup and destination locations
        googleMap.addMarker(MarkerOptions().position(pickupLatLng).title("Pickup Location"))
        googleMap.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))

        // Move the camera to focus on the pickup location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15f))
    }
}