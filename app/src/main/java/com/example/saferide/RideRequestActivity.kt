package com.example.saferide

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class RideRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var pickupLocationEditText: EditText
    private lateinit var destinationEditText: EditText
    private lateinit var requestRideButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_request)

        pickupLocationEditText = findViewById(R.id.pickupLocationEditText)
        destinationEditText = findViewById(R.id.destinationEditText)
        requestRideButton = findViewById(R.id.requestRideButton)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        database = FirebaseDatabase.getInstance().reference

        requestRideButton.setOnClickListener {
            val pickupLocation = pickupLocationEditText.text.toString()
            val destination = destinationEditText.text.toString()
            requestRide(pickupLocation, destination)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

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

    private fun requestRide(pickupLocation: String, destination: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val rideRef = database.child("rideRequests").push()
        val rideId = rideRef.key

        val rideRequest = HashMap<String, Any>()
        rideRequest["userId"] = userId!!
        rideRequest["pickupLocation"] = pickupLocation
        rideRequest["destination"] = destination
        rideRequest["status"] = "pending"

        rideRef.setValue(rideRequest)
            .addOnSuccessListener {
                // Ride request sent successfully
                // Navigate to a waiting screen or show a success message
                showSuccessMessage()
                navigateToWaitingScreen()
            }
            .addOnFailureListener {
                // Handle the failure case
                showErrorMessage(it.message)
            }
    }
    private fun showSuccessMessage() {
        Toast.makeText(this, "Ride request sent successfully", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToWaitingScreen() {
        val intent = Intent(this, WaitingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showErrorMessage(errorMessage: String?) {
        Toast.makeText(this, "Failed to send ride request: $errorMessage", Toast.LENGTH_SHORT).show()
    }
}