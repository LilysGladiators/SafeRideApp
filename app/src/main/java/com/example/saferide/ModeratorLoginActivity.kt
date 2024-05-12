package com.example.saferide

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ModeratorLoginActivity : AppCompatActivity() {
    private lateinit var gEmail: EditText
    private lateinit var gPassword: EditText
    private lateinit var gLogin: Button
    private lateinit var gAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderator_login)

        gAuth = FirebaseAuth.getInstance()

        // Initialize the views
        gEmail = findViewById(R.id.email)
        gPassword = findViewById(R.id.password)
        gLogin = findViewById(R.id.login)

        // Set click listener for the login button
        gLogin.setOnClickListener {
            val email = gEmail.text.toString()
            val password = gPassword.text.toString()
            gAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Check if the logged-in user is a moderator
                        val user = gAuth.currentUser
                        if (user != null && user.email == "moderator@example.com") {
                            // Navigate to the com.example.saferide.ModeratorActivity for moderators
                            val intent = Intent(this@ModeratorLoginActivity, ModeratorActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Navigate to the CustomerActivity for regular users
                            val intent = Intent(this@ModeratorLoginActivity, CustomerActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@ModeratorLoginActivity, "Login error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

