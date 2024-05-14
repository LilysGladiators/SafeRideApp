package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StartAndCompleteRidesActivity : AppCompatActivity() {
    private lateinit var startRideButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_and_complete_rides)

        startRideButton = findViewById(R.id.startRideButton)

        // Get the ride information from the intent extras
        val pickupLocation = intent.getStringExtra("pickupLocation")
        val destination = intent.getStringExtra("destination")

        startRideButton.setOnClickListener {
            // Start the ride and navigate to the RideInProgressActivity
            val intent = Intent(this, RideInProgressActivity::class.java)
            intent.putExtra("pickupLocation", pickupLocation)
            intent.putExtra("destination", destination)
            startActivity(intent)
            finish()
        }
    }
}