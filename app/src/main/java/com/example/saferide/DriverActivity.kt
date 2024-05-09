package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class DriverActivity : AppCompatActivity() {

    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var navigationButton: Button

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

        slider.itemAdapter.add(
            item1,
            DividerDrawerItem(),
            item2
        )

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
            Toast.makeText(this, "Accept Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to accept a ride
        }

        buttonCancelRide.setOnClickListener {
            Toast.makeText(this, "Cancelling Ride...", Toast.LENGTH_SHORT).show()
            // Add actual functionality here to cancel a ride
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
