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
import android.content.ContentValues.TAG
import android.os.CountDownTimer
import android.util.Log
import com.tomtom.quantity.Speed
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle

//Standby/idle parking spot
private val standby = GeoPoint(44.32295, -93.971697)

// Student dorms
private val southwest = GeoPoint(44.322973, -93.974788)
private val norelius = GeoPoint(44.326243, -93.969498)
private val plex = GeoPoint(44.324117, -93.968307)
private val uhler = GeoPoint(44.324324, -93.969251)
private val rundy = GeoPoint(44.321645, -93.969691)
private val collegeView = GeoPoint(44.327609, -93.973017)
private val chapelView = GeoPoint(44.331155, -93.979626)
private val prarieView = GeoPoint(44.321484, -93.974808)
private val internationalCenter = GeoPoint(44.32282, -93.97424)
private val sohrePittman = GeoPoint(44.320125, -93.97277)

// Acedemic buildings
private val campusCenter = GeoPoint(44.324442, -93.970025)
private val olin = GeoPoint(44.322628, -93.972716)
private val beck = GeoPoint(44.324109, -93.972062)
private val chapel = GeoPoint(44.32295, -93.971697)
private val mattson = GeoPoint(44.321461, -93.974808)
private val music = GeoPoint(44.320739, -93.974653)
private val art = GeoPoint(44.32021, -93.9735)
private val anderson = GeoPoint(44.321611, -93.971558)
private val nobel = GeoPoint(44.322566, -93.972019)
private val lund = GeoPoint(44.325084, -93.971043)
private val library = GeoPoint(44.32295, -93.971697)
private val oldMain = GeoPoint(44.32295, -93.971697)
private val convick = GeoPoint(44.320225, -93.972502)
private val arb = GeoPoint(44.320271, -93.974916)
private val carlson = GeoPoint(44.324439, -93.969648)

// Off-campus houses
private val petersonHouse = GeoPoint(44.321016, -93.969358)
private val sjostromHouse = GeoPoint(44.320064, -93.970259)
private val walkerHouse = GeoPoint(44.320540, -93.969755)
private val tenHouse = GeoPoint(44.320540, -93.969755)
private val adolphsonHouse = GeoPoint(44.320540, -93.969755)

private val routePlanningOptions = RoutePlanningOptions(
    itinerary = Itinerary(rundy, prarieView),
    vehicle = Vehicle.Van(maxSpeed = Speed.milesPerHour(15))
)

private var plannedRoute: Route? = null
private lateinit var countDownTimer: CountDownTimer

class EtaActivity: AppCompatActivity() {

    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var text: TextView
    private lateinit var requestRideButton: Button
    private lateinit var etaText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eta)

        // Need to create some sort of username creation system likely?
        //val user = FirebaseAuth.getInstance().currentUser.toString()
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()

        val item1 = PrimaryDrawerItem().apply { nameRes = R.string.drawer_item_home; identifier = 1 }
        val item2 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_order; identifier = 2 }
        val item3 = SecondaryDrawerItem().apply { nameRes = R.string.drawer_item_eta; identifier = 3 }

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
            item3
        )

        slider.setSelection(3)

        text = findViewById(R.id.navigationButton)
        text.setOnClickListener {
            slider.drawerLayout?.openDrawer(slider)
        }

        slider.onDrawerItemClickListener = { v, drawerItem, position ->
            // do something with clicked item :D
            when (drawerItem) {
                item1 -> startHomeActivity()
                item2 -> startCustomerActivity()
                //item3 -> this is that activity
            }
            false
        }

        /*requestRideButton = findViewById(R.id.requestRideButton)
        requestRideButton.setOnClickListener {
            startRideRequestActivity()
        }*/

        val apiKey = "zGahvvyGYB1wCGbvdIrczIROglVCrOh2"
        val routePlanner = OnlineRoutePlanner.create(applicationContext, apiKey)

        val result = routePlanner.planRoute(
            routePlanningOptions,
            object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    Log.d(TAG, "onSuccess called")
                }

                override fun onFailure(failure: RoutingFailure) {
                    Log.d(TAG, "onFailure called")
                }

                override fun onRoutePlanned(route: Route) {
                    Log.d(TAG, "onRoutePlanned called")
                    Log.d(TAG, route.summary.travelTime.toString())
                    plannedRoute = route
                    runOnUiThread {
                        updateUIWithRouteInfo()
                    }
                }
            }
        )
        etaText = findViewById(R.id.etaText)
    }

    private fun updateUIWithRouteInfo() {
        plannedRoute?.let {
            etaText.text = it.summary.travelTime.toString()
        }
        val initialTimeString = plannedRoute?.summary?.travelTime.toString()
        val milliseconds = stringToMilliseconds(initialTimeString)
        startCountdown(milliseconds)
    }

    private fun stringToMilliseconds(timeString: String): Long {
        val parts = timeString.split(" ")
        if (parts.size == 2) {
            val minutes = parts[0].replace("m", "").toLong()
            val seconds = parts[1].replace("s", "").toLong()
            return (minutes * 60 + seconds) * 1000
        } else if (parts.size == 1 && parts[0].endsWith("m")) {
            val minutes = parts[0].replace("m", "").toLong()
            return minutes * 60 * 1000
        } else if (parts.size == 1 && parts[0].endsWith("s")) {
            val seconds = parts[0].replace("s", "").toLong()
            return seconds * 1000
        } else {
            throw IllegalArgumentException("Invalid time format")
        }
    }

    private fun startCountdown(milliseconds: Long) {
        countDownTimer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val minutes = secondsRemaining / 60
                //val seconds = secondsRemaining % 60
                if (minutes < 1) {
                    val formattedTime = String.format("Less than 1 minute remaining")
                    etaText.text = formattedTime
                } else if (minutes > 1) {
                    val formattedTime = String.format("About %d minutes remaining", minutes)
                    etaText.text = formattedTime
                } else {
                    val formattedTime = String.format("About 1 minute remaining")
                    etaText.text = formattedTime
                }
            }

            override fun onFinish() {
                etaText.text = getString(R.string.arrivalText)
            }
        }
        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }


    private fun startRideRequestActivity() {
        val intent = Intent(this, RideRequestActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startCustomerActivity() {
        val intent = Intent(this, CustomerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

}