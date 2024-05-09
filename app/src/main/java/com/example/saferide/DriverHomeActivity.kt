package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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


class DriverHomeActivity: AppCompatActivity() {

    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var button: Button
    private lateinit var welcomeText: TextView
    private lateinit var requestRideButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_activity_home)

        // Need to create some sort of username creation system likely?
        val user = FirebaseAuth.getInstance().currentUser?.email.toString()
        //val email = FirebaseAuth.getInstance().currentUser?.email.toString()

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

        slider.setSelection(1)

        button = findViewById(R.id.navigationButton)
        welcomeText = findViewById(R.id.welcomeText)
        button.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { _, drawerItem, _ ->
            // do something with clicked item :D
            when (drawerItem) {
                //item1 -> is this view
                item2 -> startDriverActivity()
            }
            false
        }

        /*requestRideButton = findViewById(R.id.requestRideButton)
        requestRideButton.setOnClickListener {
            startRideRequestActivity()
        }*/

    }

    private fun startRideRequestActivity() {
        val intent = Intent(this, RideRequestActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startDriverActivity() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startMapActivity() {
        val intent = Intent(this, EtaActivity::class.java)
        startActivity(intent)
        finish()
    }

}