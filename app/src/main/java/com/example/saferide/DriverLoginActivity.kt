package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DriverLoginActivity : AppCompatActivity() {
    private lateinit var gEmail: EditText
    private lateinit var gPassword: EditText
    private lateinit var gLogin: Button
    private lateinit var gRegister: Button
    private lateinit var gAuth: FirebaseAuth
    private lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        gAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                val intent = Intent(this@DriverLoginActivity, MapActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Initialize the views
        gEmail = findViewById(R.id.email)
        gPassword = findViewById(R.id.password)
        gLogin = findViewById(R.id.login)
        gRegister = findViewById(R.id.registration)

        // Set click listener for the register button
        gRegister.setOnClickListener {
            val email = gEmail.text.toString()
            val password = gPassword.text.toString()

            gAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@DriverLoginActivity, "Sign up error", Toast.LENGTH_SHORT).show()
                    } else {
                        val userId = gAuth.currentUser?.uid
                        val currentUserDb = FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child("Drivers")
                            .child(userId!!)
                        currentUserDb.setValue(true)
                    }
                }
        }

        gLogin.setOnClickListener{
            val email = gEmail.text.toString()
            val password = gPassword.text.toString()

            gAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@DriverLoginActivity, "Sign in error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        gAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        gAuth.removeAuthStateListener(firebaseAuthListener)
    }
}