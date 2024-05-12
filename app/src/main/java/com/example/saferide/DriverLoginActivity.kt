package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DriverLoginActivity : AppCompatActivity() {
    private lateinit var gEmail: EditText
    private lateinit var gPassword: EditText
    private lateinit var gLogin: Button
    private lateinit var gAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        gAuth = FirebaseAuth.getInstance()

        // Initialize the views
        gEmail = findViewById(R.id.email)
        gPassword = findViewById(R.id.password)
        gLogin = findViewById(R.id.login)

        // Set click listener for the login button
        gLogin.setOnClickListener {
            val email = gEmail.text.toString()
            val password = gPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                gAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login successful, navigate to the DriverHomeActivity
                            val intent = Intent(this@DriverLoginActivity, DriverHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@DriverLoginActivity, "Login error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@DriverLoginActivity, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}