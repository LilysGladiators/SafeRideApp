package com.example.saferide

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var sDriver: Button
    private lateinit var sCustomer: Button
    private lateinit var sModerator: Button
    private lateinit var gAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        Log.d(TAG, "onCreate: Initializing Firebase")
        FirebaseApp.initializeApp(this)
        // Initialize Firebase Auth
        Log.d(TAG, "onCreate: Initializing Firebase Auth")
        gAuth = FirebaseAuth.getInstance()

        sDriver = findViewById(R.id.studentdriver)
        sCustomer = findViewById(R.id.studentcustomer)
        sModerator = findViewById(R.id.studentmoderator)

        // Check user's authentication state
        val currentUser = gAuth.currentUser
        if (currentUser != null) {
            // User is already registered, navigate to the appropriate login screen
            checkUserRole(currentUser.uid)
        } else {
            // User is not registered, show the driver and customer buttons
            showRoleButtons()
        }
    }

    private fun showRoleButtons() {
        sModerator.setOnClickListener {
            val currentUser = gAuth.currentUser
            if (currentUser != null) {
                // User is already registered, navigate to the DriverLoginActivity
                Log.d(TAG, "sModerator onClick: User is already registered, navigating to ModeratorLoginActivity")
                navigateToModeratorLogin()
            } else {
                // User is not registered, navigate to the DriverRegisterActivity
                Log.d(TAG, "sModerator onClick: User is not registered, navigating to ModeratorRegisterActivity")
                navigateToModeratorRegistration()
            }
        }

        sDriver.setOnClickListener {
            val currentUser = gAuth.currentUser
            if (currentUser != null) {
                // User is already registered, navigate to the DriverLoginActivity
                Log.d(TAG, "sDriver onClick: User is already registered, navigating to DriverLoginActivity")
                navigateToDriverLogin()
            } else {
                // User is not registered, navigate to the DriverRegisterActivity
                Log.d(TAG, "sDriver onClick: User is not registered, navigating to DriverRegisterActivity")
                navigateToDriverRegistration()
            }
        }

        sCustomer.setOnClickListener {
            val currentUser = gAuth.currentUser
            if (currentUser != null) {
                // User is already registered, navigate to the CustomerLoginActivity
                Log.d(TAG, "sCustomer onClick: User is already registered, navigating to CustomerLoginActivity")
                navigateToCustomerLogin()
            } else {
                // User is not registered, navigate to the CustomerRegisterActivity
                Log.d(TAG, "sCustomer onClick: User is not registered, navigating to CustomerRegisterActivity")
                navigateToCustomerRegistration()
            }
        }
    }

    private fun checkUserRole(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.child("Drivers").child(userId).get().addOnSuccessListener { driverSnapshot ->
            if (driverSnapshot.exists()) {
                // User is registered as a driver, navigate to the DriverLoginActivity
                navigateToDriverLogin()
                Log.d(TAG, "Driver login")
                // ******MAKE SURE TO ADD THIS BACK IN WHEN DONE DEBUGGING********
                navigateTFOuttaLogin()
            } else {
                userRef.child("Customers").child(userId).get().addOnSuccessListener { customerSnapshot ->
                    if (customerSnapshot.exists()) {
                        // User is registered as a customer, navigate to the CustomerLoginActivity
                        navigateToCustomerLogin()
                        Log.d(TAG, "Customer Login")
                        // ADD BACK AS WELL ****************
                        navigateTFOuttaCustomerLogin()
                    } else {
                        // User is not registered as either driver or customer, show the driver and customer buttons
                        showRoleButtons()
                    }
                }
            }
        }
    }

    fun navigateToDriverLogin() {
        Log.d(TAG, "navigateToDriverLogin: Navigating to DriverLoginActivity")
        val intent = Intent(this, DriverLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToCustomerLogin() {
        Log.d(TAG, "navigateToCustomerLogin: Navigating to CustomerLoginActivity")
        val intent = Intent(this, CustomerLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Sam added just so I don't have to log in every time I'm testing lol

   private fun navigateTFOuttaLogin() {
        val intent = Intent(this, DriverHomeActivity::class.java)
        startActivity(intent)
        finish()
   }

    private fun navigateTFOuttaCustomerLogin() {
       val intent = Intent(this, CustomerActivity::class.java)
        startActivity(intent)
        finish()
   }

    fun navigateToDriverRegistration() {
        Log.d(TAG, "navigateToDriverRegistration: Navigating to DriverRegisterActivity")
        val intent = Intent(this, DriverRegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToCustomerRegistration() {
        Log.d(TAG, "navigateToCustomerRegistration: Navigating to CustomerRegisterActivity")
        val intent = Intent(this, CustomerRegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToModeratorRegistration() {
        val intent = Intent(this, ModeratorRegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun navigateToModeratorLogin() {
        val intent = Intent(this, ModeratorLoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}