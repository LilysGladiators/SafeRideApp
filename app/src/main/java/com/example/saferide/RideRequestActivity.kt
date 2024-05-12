package com.example.saferide

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import com.tomtom.sdk.location.GeoPoint

class RideRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var pickupLocationSpinner: Spinner
    private lateinit var destinationSpinner: Spinner
    private lateinit var requestRideButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val pickupLocations = mapOf(
        "StandBy" to GeoPoint(44.32295, -93.971697),
        "Southwest" to GeoPoint(44.322973, -93.974788),
        "Norelius" to GeoPoint(44.326243, -93.969498),
        "Plex" to GeoPoint(44.324117, -93.968307),
        "Uhler" to GeoPoint(44.324324, -93.969251),
        "Rundy" to GeoPoint(44.321645, -93.969691),
        "CollegeView" to GeoPoint(44.327609, -93.973017),
        "ChapelView" to GeoPoint(44.331155, -93.979626),
        "PrairieView" to GeoPoint(44.321484, -93.974808),
        "International Center" to GeoPoint(44.32282, -93.97424),
        "SohrePittman" to GeoPoint(44.320125, -93.97277),
        "Campus Center" to GeoPoint(44.324442, -93.970025),
        "Olin" to GeoPoint(44.322628, -93.972716),
        "Beck" to GeoPoint(44.324109, -93.972062),
        "Chapel" to GeoPoint(44.32295, -93.971697),
        "Mattson" to GeoPoint(44.321461, -93.974808),
        "Music Building" to GeoPoint(44.320739, -93.974653),
        "Art Building" to GeoPoint(44.32021, -93.9735),
        "Anderson" to GeoPoint(44.321611, -93.971558),
        "Nobel" to GeoPoint(44.322566, -93.972019),
        "Lund" to GeoPoint(44.325084, -93.971043),
        "Library" to GeoPoint(44.32295, -93.971697),
        "OldMain" to GeoPoint(44.32295, -93.971697),
        "ConVick" to GeoPoint(44.320225, -93.972502),
        "Arb" to GeoPoint(44.320271, -93.974916),
        "Carlson" to GeoPoint(44.324439, -93.969648),
        "Peterson House" to GeoPoint(44.321016, -93.969358),
        "Sjostrom House" to GeoPoint(44.320064, -93.970259),
        "Walker House" to GeoPoint(44.320540, -93.969755),
        "Ten House" to GeoPoint(44.320540, -93.969755),
        "Adolphson House" to GeoPoint(44.320540, -93.969755)
    )

    private val destinationLocations = mapOf(
        "StandBy" to GeoPoint(44.32295, -93.971697),
        "Southwest" to GeoPoint(44.322973, -93.974788),
        "Norelius" to GeoPoint(44.326243, -93.969498),
        "Plex" to GeoPoint(44.324117, -93.968307),
        "Uhler" to GeoPoint(44.324324, -93.969251),
        "Rundy" to GeoPoint(44.321645, -93.969691),
        "CollegeView" to GeoPoint(44.327609, -93.973017),
        "ChapelView" to GeoPoint(44.331155, -93.979626),
        "PrairieView" to GeoPoint(44.321484, -93.974808),
        "International Center" to GeoPoint(44.32282, -93.97424),
        "SohrePittman" to GeoPoint(44.320125, -93.97277),
        "Campus Center" to GeoPoint(44.324442, -93.970025),
        "Olin" to GeoPoint(44.322628, -93.972716),
        "Beck" to GeoPoint(44.324109, -93.972062),
        "Chapel" to GeoPoint(44.32295, -93.971697),
        "Mattson" to GeoPoint(44.321461, -93.974808),
        "Music Building" to GeoPoint(44.320739, -93.974653),
        "Art Building" to GeoPoint(44.32021, -93.9735),
        "Anderson" to GeoPoint(44.321611, -93.971558),
        "Nobel" to GeoPoint(44.322566, -93.972019),
        "Lund" to GeoPoint(44.325084, -93.971043),
        "Library" to GeoPoint(44.32295, -93.971697),
        "OldMain" to GeoPoint(44.32295, -93.971697),
        "ConVick" to GeoPoint(44.320225, -93.972502),
        "Arb" to GeoPoint(44.320271, -93.974916),
        "Carlson" to GeoPoint(44.324439, -93.969648),
        "Peterson House" to GeoPoint(44.321016, -93.969358),
        "Sjostrom House" to GeoPoint(44.320064, -93.970259),
        "Walker House" to GeoPoint(44.320540, -93.969755),
        "Ten House" to GeoPoint(44.320540, -93.969755),
        "Adolphson House" to GeoPoint(44.320540, -93.969755)
    )

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_request)

        pickupLocationSpinner = findViewById(R.id.pickupLocationSpinner)
        destinationSpinner = findViewById(R.id.destinationSpinner)
        requestRideButton = findViewById(R.id.requestRideButton)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        database = FirebaseDatabase.getInstance().reference

        requestRideButton.setOnClickListener {
            requestRide()
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

    private fun requestRide() {
        val selectedPickupLocation = pickupLocationSpinner.selectedItem.toString()
        val pickupGeoPoint = pickupLocations[selectedPickupLocation]

        val selectedDestinationLocation = destinationSpinner.selectedItem.toString()
        val destinationGeoPoint = destinationLocations[selectedDestinationLocation]

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val rideRef = database.child("rideRequests").push()
        val rideId = rideRef.key

        val rideRequest = HashMap<String, Any>()
        rideRequest["userId"] = userId!!
        rideRequest["pickupLocation"] = selectedPickupLocation
        rideRequest["pickupLatitude"] = pickupGeoPoint?.latitude ?: 0.0
        rideRequest["pickupLongitude"] = pickupGeoPoint?.longitude ?: 0.0
        rideRequest["destinationLocation"] = selectedDestinationLocation
        rideRequest["destinationLatitude"] = destinationGeoPoint?.latitude ?: 0.0
        rideRequest["destinationLongitude"] = destinationGeoPoint?.longitude ?: 0.0
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

    private fun startMapActivity() {
        val intent = Intent(this, EtaActivity::class.java)
        startActivity(intent)
        finish()
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