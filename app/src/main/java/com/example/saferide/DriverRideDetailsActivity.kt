package com.example.saferide

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DriverRideDetailsActivity : AppCompatActivity() {

    private lateinit var gDatabase: FirebaseDatabase
    private lateinit var gRideDetailsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_ride_details)

        gDatabase = FirebaseDatabase.getInstance()
        gRideDetailsTextView = findViewById(R.id.ride_details_text_view)

        val rideId = intent.getStringExtra("rideId")
        if (rideId != null) {
            val rideRef = gDatabase.getReference("rides").child(rideId)
            rideRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val ride = snapshot.getValue(RideActivity::class.java)
                        val rideDetails = "Pickup Location: ${ride?.pickupLocation}\n" +
                                "Destination: ${ride?.destinationLocation}\n"
                        gRideDetailsTextView.text = rideDetails
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    val errorMessage = when (error.toException()) {
                        is FirebaseNetworkException -> "Network error occurred. Please check your internet connection."
                        is FirebaseAuthException -> "Authentication error occurred. Please log in again."
                        is FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
                        else -> "An error occurred. Please try again later."
                    }

                    // Display the error message to the user
                    Toast.makeText(this@DriverRideDetailsActivity, errorMessage, Toast.LENGTH_LONG).show()

                    // Log the error for debugging purposes
                    Log.e("DriverRideDetailsActivity", "Database error: ${error.message}", error.toException())
                }
            })
        }
    }
}