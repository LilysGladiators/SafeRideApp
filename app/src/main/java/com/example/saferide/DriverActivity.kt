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

        slider = findViewById(R.id.slider)
        navigationButton = findViewById(R.id.navigationButton)

        slider.headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply { nameText = "Student Driver"; descriptionText = user; iconRes = R.drawable.ic_launcher_foreground; identifier = 102}
            )
            onAccountHeaderListener = { view, profile, current ->
                //react to profile changes
                false
            }
            withSavedInstance(savedInstanceState)
        }

        slider.itemAdapter.add(
            item1,
            DividerDrawerItem(),
            item2
        )

        slider.setSelection(2)

        navigationButton.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            // do something with clicked item :D
            when (drawerItem) {
                item1 -> startDriverHomeActivity()
                item2 -> startDriverActivity()
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
                                "Destination: ${rideRequest?.destination}"
                        gRideDetailsTextView.text = rideDetails

                        // Set click listener for the accept button
                        gAcceptButton.setOnClickListener {
                            // Update ride status to "accepted"
                            val rideRef = gDatabase.getReference("rides").push()
                            rideRef.child("status").setValue("accepted")
                            rideRef.child("driverId").setValue(currentUser.uid)

                            // Remove the ride requests
                            snapshot.ref.removeValue()

                            // Navigate to the ride details activity
                            val intent = Intent(this@DriverActivity, DriverRideDetailsActivity::class.java)
                            intent.putExtra("rideId", rideRef.key)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // No ride request found, display waiting message
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
        }
    }

    private fun startDriverHomeActivity() {
        val intent = Intent(this, DriverHomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startDriverActivity() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finish()
    }
}