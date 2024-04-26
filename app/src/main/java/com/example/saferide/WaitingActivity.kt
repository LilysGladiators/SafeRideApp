package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WaitingActivity : AppCompatActivity() {

    private lateinit var waitingMessageTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var rideRequestEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        waitingMessageTextView = findViewById(R.id.waitingMessageTextView)

        database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        rideRequestEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (rideRequestSnapshot in snapshot.children) {
                        val rideRequest = rideRequestSnapshot.getValue(RideRequest::class.java)
                        if (rideRequest != null) {
                            if (rideRequest?.userId == userId && rideRequest.status == "accepted") {
                                // Ride request has been accepted, navigate to the navigation screen
                                val rideId = rideRequestSnapshot.key
                                navigateToNavigationScreen(rideId)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WaitingActivity", "Database error: ${error.message}")
                showErrorMessage(error.message)
            }
        }

        database.child("rideRequests").addValueEventListener(rideRequestEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        database.child("rideRequests").removeEventListener(rideRequestEventListener)
    }

    private fun navigateToNavigationScreen(rideId: String?) {
        if (rideId != null) {
            val intent = Intent(this, NavigationActivity::class.java)
            intent.putExtra("rideId", rideId)
            startActivity(intent)
            finish()
        }
    }

    private fun showErrorMessage(errorMessage: String?) {
        if (errorMessage != null) {
            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "An unknown error occurred", Toast.LENGTH_LONG).show()
        }
    }
}