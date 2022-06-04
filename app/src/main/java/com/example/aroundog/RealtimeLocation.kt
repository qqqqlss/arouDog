package com.example.aroundog

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue

import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.naver.maps.geometry.LatLng
class RealtimeLocation {
    private lateinit var database: DatabaseReference
    private lateinit var ruser:User
    var lat=0.0
    var lng=0.0
    fun initializeDbRef() { //초기화
        database = Firebase.database.reference
    }
    fun writeNewUser(userId: String, Lat: Double, Lng: Double) {
        val user = User(Lat, Lng)
        database.child("users").child(userId).setValue(user)
    }

    fun getValue(userId: String):LatLng {
        var a = HashMap<String ,Any?>() ;
        database.child("users").child(userId).get().addOnSuccessListener {
            a = it.value as HashMap<String, Any?>
            Log.e("firebase" , a.toString() + a?.javaClass?.name.toString())
            lat= a.get("lat") as Double
            lng= a.get("lng") as Double
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
        return LatLng(lat,lng)
    }
}


@IgnoreExtraProperties
data class User(val lat: Double? = null, val lng: Double? = null) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

}