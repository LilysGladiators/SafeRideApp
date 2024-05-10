package com.example.saferide

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferide.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ModeratorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderator)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize buttons
        val buttonAddDriver = findViewById<Button>(R.id.buttonAddDriver)
        val buttonRemoveDriver = findViewById<Button>(R.id.buttonRemoveDriver)

        // Set onClick listeners for the buttons
        buttonAddDriver.setOnClickListener {
            // Call method to add driver
            addDriver("DriverName", "DriverLicense")
        }

        buttonRemoveDriver.setOnClickListener {
            // Call method to remove driver
            removeDriver("DriverId")
        }
    }

    // Method to add a new driver
    private fun addDriver(driverName: String, driverLicense: String) {
        // Implement adding driver to Firebase Realtime Database or Firestore
        Toast.makeText(this, "Adding Driver...", Toast.LENGTH_SHORT).show()
    }

    // Method to remove an existing driver
    private fun removeDriver(driverId: String) {
        // Implement removing driver from Firebase Realtime Database or Firestore
        Toast.makeText(this, "Removing Driver...", Toast.LENGTH_SHORT).show()
    }
}

