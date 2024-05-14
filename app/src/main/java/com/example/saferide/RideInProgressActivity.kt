package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RideInProgressActivity : AppCompatActivity() {
    private lateinit var rideStatusTextView: TextView
    private lateinit var completeRideButton: Button
    private lateinit var pickupLocationTextView: TextView
    private lateinit var destinationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_in_progress)

        //rideStatusTextView = findViewById(R.id.rideStatusTextView)
        completeRideButton = findViewById(R.id.completeRideButton)
        pickupLocationTextView = findViewById(R.id.pickupLocationTextView)
        destinationTextView = findViewById(R.id.destinationTextView)

        // Get the ride information from the intent extras
        val pickupLocation = intent.getStringExtra("pickupLocation")
        val destination = intent.getStringExtra("destination")

        // Display the ride information
        pickupLocationTextView.text = "Pickup: $pickupLocation"
        destinationTextView.text = "Destination: $destination"

        completeRideButton.setOnClickListener {
            // Update the ride status to "Ride Completed"
            //rideStatusTextView.text = "Ride Completed"

            // Navigate back to the DriverHomeActivity after a short delay
            completeRideButton.postDelayed({
                val intent = Intent(this, RideCompleteActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000) // Delay of 1000 milliseconds (1 second)
        }
    }
}