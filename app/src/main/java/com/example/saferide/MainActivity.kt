package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var sDriver: Button
    private lateinit var sCustomer: Button
    private lateinit var gAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Auth
        gAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

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

    private fun checkUserRole(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.child("Drivers").child(userId).get().addOnSuccessListener { driverSnapshot ->
            if (driverSnapshot.exists()) {
                // User is registered as a driver, navigate to the DriverLoginActivity
                navigateToDriverLogin()
            } else {
                userRef.child("Customers").child(userId).get().addOnSuccessListener { customerSnapshot ->
                    if (customerSnapshot.exists()) {
                        // User is registered as a customer, navigate to the CustomerLoginActivity
                        navigateToCustomerLogin()
                    } else {
                        // User is not registered as either driver or customer, show the driver and customer buttons
                        showRoleButtons()
                    }
                }
            }
        }
    }

    private fun navigateToDriverLogin() {
        val intent = Intent(this, DriverLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToCustomerLogin() {
        val intent = Intent(this, CustomerLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showRoleButtons() {
        setContentView(R.layout.activity_main)

        sDriver = findViewById(R.id.studentdriver)
        sCustomer = findViewById(R.id.studentcustomer)

        sDriver.setOnClickListener {
            val intent = Intent(this@MainActivity, DriverRegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        sCustomer.setOnClickListener {
            val intent = Intent(this@MainActivity, CustomerRegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}