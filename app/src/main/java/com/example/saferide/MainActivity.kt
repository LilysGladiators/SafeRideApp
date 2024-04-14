package com.example.saferide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var sDriver: Button
    private lateinit var sCustomer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sDriver = findViewById(R.id.studentdriver)
        sCustomer = findViewById(R.id.studentcustomer)

        sDriver.setOnClickListener {
            val intent = Intent(this@MainActivity, DriverLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        sCustomer.setOnClickListener {
            val intent = Intent(this@MainActivity, CustomerLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
