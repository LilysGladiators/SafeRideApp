package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RideCompleteActivity : AppCompatActivity() {
    private lateinit var pickupLocationTextView: TextView
    private lateinit var destinationTextView: TextView
    private lateinit var goToHomeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_complete)

        pickupLocationTextView = findViewById(R.id.pickupLocationTextView)
        destinationTextView = findViewById(R.id.destinationTextView)
        goToHomeButton = findViewById(R.id.goToHomeButton)

        // Get the ride information from the intent extras
        val pickupLocation = intent.getStringExtra("pickupLocation")
        val destination = intent.getStringExtra("destination")

        // Display the ride summary
        pickupLocationTextView.text = "Pickup: $pickupLocation"
        destinationTextView.text = "Destination: $destination"

        goToHomeButton.setOnClickListener {
            // Navigate to the DriverHomeActivity
            val intent = Intent(this, DriverHomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}