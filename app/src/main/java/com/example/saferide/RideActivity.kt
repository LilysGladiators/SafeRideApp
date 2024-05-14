package com.example.saferide

data class RideActivity(
    val userId: String = "",
    val pickupLocation: String = "",
    val destinationLocation: String = "",
    var driverFound: Boolean = false
) {
    constructor() : this("", "", "", false) // No-argument constructor
}