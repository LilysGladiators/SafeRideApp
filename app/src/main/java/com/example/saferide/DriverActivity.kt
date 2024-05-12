package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FieldValue
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

    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var navigationButton: Button
    private var currentStudent: Map<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        val user = FirebaseAuth.getInstance().currentUser?.email.toString()
        //val email = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.drawer_item_home; identifier = 1 }
        val item2 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_order; identifier = 2 }
        //val item3 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_eta; identifier = 3 }

        slider = findViewById(R.id.slider)

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

        slider.itemAdapter.add(item1,DividerDrawerItem(),item2)
        slider.setSelection(2)

        navigationButton = findViewById(R.id.navigationButton)
        navigationButton.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            // do something with clicked item :D
            when (drawerItem) {
                item1 -> startDriverHomeActivity()
                item2 -> startDriverActivity()
                //item3 -> startMapActivity()
            }
            false
        }

        // Initialize buttons
        val buttonAcceptRide = findViewById<Button>(R.id.buttonAcceptRide)
        val buttonCancelRide = findViewById<Button>(R.id.buttonCancelRide)

        // Set onClick listeners for the buttons
        buttonAcceptRide.setOnClickListener {
            currentStudent?.let {
                removeFromQueue(it)
                Toast.makeText(this, "Ride Accepted!", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to accept a ride
            } ?: Toast.makeText(this, "No ride to accept", Toast.LENGTH_SHORT).show()
        }

        buttonCancelRide.setOnClickListener {
            currentStudent?.let {
                removeFromQueue(it)
                Toast.makeText(this, "Ride Cancelled!", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to cancel a ride
            } ?: Toast.makeText(this, "No ride to cancel", Toast.LENGTH_SHORT).show()
        }
        // Fetch the next student in the queue when activity starts
        fetchNextStudentInQueue()
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
                Toast.makeText(this, "No ride requests in currently", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{ e ->
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
            }.addOnFailureListener{ e ->
                Toast.makeText(this, "Failed to remove student: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
