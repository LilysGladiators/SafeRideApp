package com.example.saferide

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class DriverRegisterActivity : AppCompatActivity() {
    private lateinit var gEmail: EditText
    private lateinit var gPassword: EditText
    private lateinit var gRegister: Button
    private lateinit var gAuth: FirebaseAuth
    private lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        gAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {

                Log.d(TAG, "User != null!!!")
                /*val intent = Intent(this@DriverRegisterActivity, CustomerActivity::class.java)
=======
                val intent = Intent(this@DriverRegisterActivity, DriverActivity::class.java)
>>>>>>> Stashed changes
                startActivity(intent)
                finish()*/
            } else {
                Log.d(TAG, "User == null!!!")
                // User is not logged in, navigate to the DriverLoginActivity
                /*val intent = Intent(this@DriverRegisterActivity, DriverLoginActivity::class.java)
                startActivity(intent)
                finish()*/
            }
        }

        // Initialize the views
        gEmail = findViewById(R.id.email)
        gPassword = findViewById(R.id.password)
        gRegister = findViewById(R.id.registration)

        // Set click listener for the register button
        gRegister.setOnClickListener {
            val email = gEmail.text.toString()
            val password = gPassword.text.toString()

            gAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = gAuth.currentUser?.uid
                        val currentUserDb = FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child("Drivers")
                            .child(userId!!)
                        currentUserDb.setValue(true)

                        // Registration successful, navigate to the LoginActivity
                        val intent = Intent(this@DriverRegisterActivity, DriverLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@DriverRegisterActivity, "Sign up error", Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}