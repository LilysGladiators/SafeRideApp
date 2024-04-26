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


class MapActivity: AppCompatActivity() {

    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var text: TextView
    private lateinit var requestRideButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Need to create some sort of username creation system likely?
        //val user = FirebaseAuth.getInstance().currentUser.toString()
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()

        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.drawer_item_home; identifier = 1 }
        val item2 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_order; identifier = 2 }
        val item3 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_waitlist; identifier = 3 }
        val item4 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_settings; identifier = 4 }

        slider = findViewById(R.id.slider)

        slider.headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                ProfileDrawerItem().apply { nameText = "USERNAME"; descriptionText = email; iconRes = R.drawable.ic_launcher_foreground; identifier = 102}
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
            item3,
            item4
        )

        text = findViewById(R.id.simpleTextView)
        text.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
            text.text = "Successful Drawer Open"
        }

        slider.onDrawerItemClickListener = { v, drawerItem, position ->
            // do something with clicked item :D
            false
        }

        requestRideButton = findViewById(R.id.requestRideButton)
        requestRideButton.setOnClickListener {
            startRideRequestActivity()
        }

    }

    private fun startRideRequestActivity() {
        val intent = Intent(this, RideRequestActivity::class.java)
        startActivity(intent)
    }
}