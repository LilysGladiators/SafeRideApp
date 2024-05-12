package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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

class DriverHomeActivity : AppCompatActivity() {
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var button: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var waitingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Need to create some sort of username creation system likely?
        val user = FirebaseAuth.getInstance().currentUser?.email?.toString()

        val item1 = PrimaryDrawerItem().apply {
            nameRes = R.string.drawer_item_home
            identifier = 1
        }
        val item2 = SecondaryDrawerItem().apply {
            nameRes = R.string.drawer_item_order
            identifier = 2
        }

        slider = findViewById(R.id.slider)
        slider.headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply {
                    nameText = "Student Driver"
                    descriptionText = "user"
                    iconRes = R.drawable.ic_launcher_foreground
                    identifier = 102
                }
            )
            onAccountHeaderListener = { view, profile, current ->
                // React to profile changes
                false
            }
            withSavedInstance(savedInstanceState)
        }
        slider.itemAdapter.add(item1, DividerDrawerItem(), item2)
        slider.setSelection(1)

        button = findViewById(R.id.navigationButton)
        waitingText = findViewById(R.id.waitingText)

        button.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            when (drawerItem) {
                item2 -> startDriverActivity()
                else -> {} // Do nothing for other items
            }
            false
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Driver is not logged in, redirect to DriverLoginActivity
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Driver is logged in, listen for ride requests
            val rideRequestsRef = database.getReference("rideRequests")
            rideRequestsRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val rideRequest = snapshot.getValue(RideRequest::class.java)
                    if (rideRequest != null) {
                        // Ride request received, navigate to DriverRideAcceptanceActivity
                        val intent = Intent(this@DriverHomeActivity, DriverActivity::class.java)
                        intent.putExtra("rideRequestId", snapshot.key)
                        startActivity(intent)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DriverHomeActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

            // Display the "... people currently waiting for rides" text
            waitingText.text = "0 people currently waiting for rides"
        }
    }

    private fun startDriverActivity() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finish()
    }
}