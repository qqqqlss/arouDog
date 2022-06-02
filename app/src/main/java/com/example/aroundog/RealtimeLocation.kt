package com.example.aroundog

import com.google.firebase.database.*
import com.google.firebase.database.ktx.database

import com.google.firebase.ktx.Firebase

class RealtimeLocation {
    private lateinit var database: DatabaseReference

    fun initializeDbRef() { //초기화
        database = Firebase.database.reference
    }
    fun writeNewUser(userId: String, Lat: Number, Lng: Number) {
        val user = User(Lat, Lng)
        database.child("users").child(userId).setValue(user)
    }

}

@IgnoreExtraProperties
data class User(val Lat: Number? = null, val Lng: Number? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}