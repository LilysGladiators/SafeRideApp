package com.example.saferide

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
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

class CustomerActivity : AppCompatActivity() {
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var text: TextView
    //private lateinit var requestRideButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        // Need to create some sort of username creation system likely?
        val user = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()

        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.drawer_item_home; identifier = 1 }
        val item2 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_order; identifier = 2 }
        val item3 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_eta; identifier = 3 }

        slider = findViewById(R.id.slider)

        slider.headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply { nameText = user; descriptionText = email; iconRes = R.drawable.ic_launcher_foreground; identifier = 102}
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
            item2,
            item3
        )

        slider.setSelection(2)

        text = findViewById(R.id.navigationButton)
        text.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            // do something with clicked item :D
            when (drawerItem) {
                item1 -> startHomeActivity()
                //item2 -> insert here
                item3 -> startMapActivity()
            }
            false
        }
        // Initialize buttons
        val buttonRequestRide = findViewById<Button>(R.id.buttonRequestRide)
        val buttonCancelRide = findViewById<Button>(R.id.buttonCancelRide)

        // Set onClick listeners for the buttons
        buttonRequestRide.setOnClickListener {
            startRideRequestActivity()
            //Toast.makeText(this, "Requesting Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to request a ride
        }

        buttonCancelRide.setOnClickListener {
            Toast.makeText(this, "Cancelling Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to cancel a ride
        }

    }

    private fun startRideRequestActivity() {
        val db = Firebase.firestore // Get an instance of Firestore
        val userEmail = FirebaseAuth.getInstance().currentUser?.email?:"Unknown User"
        val documentReference = db.collection("rides").document("currentRide")

        // Start Firestore transaction to update the users array
        db.runTransaction{ transaction ->
            val snapshot = transaction.get(documentReference)
            val users = snapshot.get("users") as? MutableList<String> ?: mutableListOf()

            // Add user email if not already included
            if(!users.contains(userEmail)){
                users.add(userEmail)
                transaction.update(documentReference, "users", users)
            }
        }.addOnSuccessListener {
            Toast.makeText(this, "Ride requested successfully!", Toast.LENGTH_SHORT).show()
            //Proceed to ride request activity
            navigateRideRequestActivity()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to request ride. Try again later.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateRideRequestActivity() {
        val intent = Intent(this, RideRequestActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startMapActivity() {
        val intent = Intent(this, EtaActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

}
