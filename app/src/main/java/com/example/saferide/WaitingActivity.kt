package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

class WaitingActivity : AppCompatActivity() {

    private lateinit var waitingMessageTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var rideRequestEventListener: ValueEventListener
    private lateinit var etaTextView: TextView
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        waitingMessageTextView = findViewById(R.id.waitingMessageTextView)
        etaTextView = findViewById(R.id.etaTextView)

        database = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        rideRequestEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (rideRequestSnapshot in snapshot.children) {
                        val rideRequest = rideRequestSnapshot.getValue(RideRequest::class.java)
                        if (rideRequest != null) {
                            if (rideRequest.userId == userId && rideRequest.status == "accepted") {
                                // Ride request has been accepted, calculate ETA and navigate to the navigation screen
                                val rideId = rideRequestSnapshot.key
                                calculateAndDisplayETA(rideRequest)
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
        countDownTimer.cancel()
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

    private fun calculateAndDisplayETA(rideRequest: RideRequest) {
        val pickupGeoPoint = GeoPoint(rideRequest.pickupLatitude, rideRequest.pickupLongitude)
        val destinationGeoPoint = GeoPoint(rideRequest.destinationLatitude, rideRequest.destinationLongitude)

        val routePlanningOptions = RoutePlanningOptions(
            itinerary = Itinerary(pickupGeoPoint, destinationGeoPoint),
            vehicle = Vehicle.Van(maxSpeed = Speed.milesPerHour(15))
        )

        val apiKey = "zGahvvyGYB1wCGbvdIrczIROg1VCrOh2"
        val routePlanner = OnlineRoutePlanner.create(applicationContext, apiKey)

        routePlanner.planRoute(routePlanningOptions, object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                Log.d(TAG, "Route planning success")
            }

            override fun onFailure(failure: RoutingFailure) {
                Log.e(TAG, "Route planning failure: ${failure.message}")
            }

            override fun onRoutePlanned(route: Route) {
                Log.d(TAG, "Route planned: ${route.summary.travelTime}")
                runOnUiThread {
                    updateUIWithRouteInfo(route)
                }
            }
        })
    }

    private fun updateUIWithRouteInfo(route: Route) {
        val initialTimeString = route.summary.travelTime.toString()
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
                if (minutes < 1) {
                    val formattedTime = String.format("Less than 1 minute remaining")
                    etaTextView.text = formattedTime
                } else if (minutes > 1) {
                    val formattedTime = String.format("About %d minutes remaining", minutes)
                    etaTextView.text = formattedTime
                } else {
                    val formattedTime = String.format("About 1 minute remaining")
                    etaTextView.text = formattedTime
                }
            }

            override fun onFinish() {
                etaTextView.text = getString(R.string.arrivalText)
            }
        }
        countDownTimer.start()
    }

    companion object {
        private const val TAG = "WaitingActivity"
    }
}