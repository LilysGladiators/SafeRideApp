package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class RideRequest(
    val userId: String = "",
    val pickupLocation: String = "",
    val destination: String = "",
    val pickupLatitude: Double = 0.0,
    val pickupLongitude: Double = 0.0,
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val status: String = "",
    val driverId: String = ""
)

class DriverMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var database: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        database = FirebaseDatabase.getInstance().reference

        database.child("rideRequests")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val rideRequest = snapshot.getValue(RideRequest::class.java)
                    if (rideRequest?.status == "pending") {
                        // Display the ride request on the map
                        displayRideRequestOnMap(snapshot.key, rideRequest)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle ride request changes if needed
                    val updatedRideRequest = snapshot.getValue(RideRequest::class.java)

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle ride request removal if needed
                    val removedRideRequest = snapshot.getValue(RideRequest::class.java)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // This event is typically not used in most cases, so you can leave it empty
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Log.e("MyChildEventListener", "Error: ${error.message}")
                }
            })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Set up the map and enable location updates
        // You can customize the map settings and add markers or overlays as needed
    }

    private fun displayRideRequestOnMap(rideId: String?, rideRequest: RideRequest?) {
        if (rideId != null && rideRequest != null) {
            // Parse the pickup location and destination coordinates from the rideRequest
            val pickupLatLng = LatLng(rideRequest.pickupLatitude, rideRequest.pickupLongitude)
            val destinationLatLng = LatLng(rideRequest.destinationLatitude, rideRequest.destinationLongitude)

            // Add markers for the pickup location and destination on the map
            map.addMarker(MarkerOptions().position(pickupLatLng).title("Pickup Location"))
            map.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))

            // Move the camera to focus on the pickup location
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 12f))

            // Set a click listener on the pickup location marker to accept the ride request
            map.setOnMarkerClickListener { marker ->
                if (marker.title == "Pickup Location") {
                    acceptRideRequest(rideId)
                }
                true
            }
        }
    }

    private fun acceptRideRequest(rideId: String) {
        val driverId = FirebaseAuth.getInstance().currentUser?.uid
        val rideRef = database.child("rideRequests").child(rideId)

        rideRef.child("driverId").setValue(driverId)
        rideRef.child("status").setValue("accepted")
            .addOnSuccessListener {
                // Ride request accepted
                showSuccessMessage()
                navigateToNavigationScreen(rideId)
            }
            .addOnFailureListener {
                // Handle the failure case
                showErrorMessage(it.message)
            }
    }

    private fun showSuccessMessage() {
        Toast.makeText(this, "Ride request accepted", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(errorMessage: String?) {
        Toast.makeText(this, "Failed to accept ride request: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToNavigationScreen(rideId: String) {
        val intent = Intent(this, NavigationActivity::class.java)
        intent.putExtra("rideId", rideId)
        startActivity(intent)
        finish()
    }
}