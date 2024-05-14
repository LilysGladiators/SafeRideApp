package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.descriptionText
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView

class DriverActivity : AppCompatActivity() {

    private lateinit var gAuth: FirebaseAuth
    private lateinit var gDatabase: FirebaseDatabase
    private lateinit var gAcceptButton: Button
    private lateinit var gRideDetailsTextView: TextView
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var navigationButton: Button
    private var currentStudent: Map<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        gAuth = FirebaseAuth.getInstance()
        gDatabase = FirebaseDatabase.getInstance()

        gAcceptButton = findViewById(R.id.buttonAcceptRide)
        gRideDetailsTextView = findViewById(R.id.ride_details_text_view)

        val user = FirebaseAuth.getInstance().currentUser?.email.toString()

        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.drawer_item_home; identifier = 1 }
        val item2 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_order; identifier = 2 }
        val item3 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_start_ride; identifier = 3 }

        slider = findViewById(R.id.slider)
        navigationButton = findViewById(R.id.navigationButton)

        slider.headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply {
                    nameText = "Student Driver"
                    descriptionText = user
                    iconRes = R.drawable.ic_launcher_foreground
                    identifier = 102
                }
            )
            onAccountHeaderListener = { _, _, _ ->
                false
            }
            withSavedInstance(savedInstanceState)
        }

        slider.itemAdapter.add(
            item1,
            DividerDrawerItem(),
            item2,
            DividerDrawerItem(),
            item3
        )

        slider.setSelection(2)

        navigationButton.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            when (drawerItem) {
                item1 -> startDriverHomeActivity()
                item2 -> startDriverActivity()
                item3 -> startStartAndCompleteRidesActivity()
            }
            false
        }

        // Check if the driver is logged in
        val currentUser = gAuth.currentUser
        if (currentUser == null) {
            // Driver is not logged in, redirect to the login activity
            val intent = Intent(this@DriverActivity, DriverLoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Driver is logged in, listen for ride requests
            val rideRequestsRef = gDatabase.getReference("rideRequests")
            rideRequestsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Ride request found
                        val rideRequest = snapshot.getValue(RideRequest::class.java)
                        val rideDetails = "Pickup Location: ${rideRequest?.pickupLocation}\n" +
                                "Destination: ${rideRequest?.destinationLocation}"
                        gRideDetailsTextView.text = rideDetails
                        // Set click listener for the accept button
                        gAcceptButton.setOnClickListener {
                            val intent = Intent(this@DriverActivity, StartAndCompleteRidesActivity::class.java)

                            currentStudent?.let {
                                removeFromQueue(it)
                                Toast.makeText(this@DriverActivity, R.string.ride_accepted, Toast.LENGTH_SHORT).show()
                                // Add actual functionality here to accept a ride
                            } ?: Toast.makeText(this@DriverActivity, "No ride to accept", Toast.LENGTH_SHORT).show()
                        }

                        // No available ride request found, display waiting message
                        gRideDetailsTextView.text = "Waiting for ride requests..."
                        gAcceptButton.isEnabled = false
                    } else {
                        // No ride requests found, display waiting message
                        gRideDetailsTextView.text = "Waiting for ride requests..."
                        gAcceptButton.isEnabled = false
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
                    Toast.makeText(this@DriverActivity, errorMessage, Toast.LENGTH_LONG).show()

                    // Log the error for debugging purposes
                    Log.e("DriverRideAcceptanceActivity", "Database error: ${error.message}", error.toException())
                }
            })

            // Fetch the next student in the queue when activity starts
            fetchNextStudentInQueue()
        }
    }

    private fun fetchNextStudentInQueue() {
        val db = Firebase.firestore
        val waitlistRef = db.collection("SafeRide_FS").document("Waitlist")

        waitlistRef.get().addOnSuccessListener { document ->
            val waitingList = document["Waiting_Students"] as? List<Map<String, Any>> ?: listOf()
            if (waitingList.isNotEmpty()) {
                val sortedList = waitingList.sortedBy { (it["timestamp"] as? com.google.firebase.Timestamp)?.seconds ?: 0L }
                currentStudent = sortedList.firstOrNull()
                currentStudent?.let {
                    Toast.makeText(this, "Next ride request: ${it["studentId"]}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No ride requests currently", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error retrieving waitlist: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromQueue(student: Map<String, Any>) {
        val db = Firebase.firestore
        val waitlistRef = db.collection("SafeRide_FS").document("Waitlist")
        waitlistRef.update("Waiting_Students", FieldValue.arrayRemove(student))
            .addOnSuccessListener {
                currentStudent = null
                Toast.makeText(this, "Student removed from queue", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to remove student: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startDriverHomeActivity() {
        val intent = Intent(this, DriverHomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startStartAndCompleteRidesActivity() {
        val intent = Intent(this, StartAndCompleteRidesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startDriverActivity() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finish()
    }
}