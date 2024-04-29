package com.example.saferide

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DriverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        // Initialize buttons
        val buttonAcceptRide = findViewById<Button>(R.id.buttonRequestRide)
        val buttonCancelRide = findViewById<Button>(R.id.buttonCancelRide)

        // Set onClick listeners for the buttons
        buttonAcceptRide.setOnClickListener {
            Toast.makeText(this, "Accept Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to request a ride
        }

        buttonCancelRide.setOnClickListener {
            Toast.makeText(this, "Cancelling Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to cancel a ride
        }
    }
}
