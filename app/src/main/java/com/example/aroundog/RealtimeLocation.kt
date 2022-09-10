package com.example.aroundog

import android.util.Log
import com.example.aroundog.Model.UpdateWalkHistory
import com.example.aroundog.dto.WalkingUserDto
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue

import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RealtimeLocation {
    val TAG = "REALTIMELOCATION"
//    private lateinit var database: DatabaseReference
//    private lateinit var ruser:User
//    var lat=0.0
//    var lng=0.0
//    fun initializeDbRef() { //초기화
//        database = Firebase.database.reference
//    }
//    fun writeNewUser(userId: String, Lat: Double, Lng: Double) {
//        val user = User(Lat, Lng)
//        database.child("users").child(userId).setValue(user)
//    }
//
//    fun getValue(userId: String):LatLng {
//        var a = HashMap<String ,Any?>() ;
//        database.child("users").child(userId).get().addOnSuccessListener {
//            a = it.value as HashMap<String, Any?>
//            Log.e("firebase" , a.toString() + a?.javaClass?.name.toString())
//            lat= a.get("lat") as Double
//            lng= a.get("lng") as Double
//        }.addOnFailureListener {
//            Log.e("firebase", "Error getting data", it)
//        }
//        return LatLng(lat,lng)
//    }

    private lateinit var database: DatabaseReference
    var lat = 0.0
    var lng = 0.0
    fun initializeDbRef() { //초기화
        database = Firebase.database.reference
    }

//    fun updateUserWithTile(userId: Long, latitude: Double, longitude: Double, tile:String) {
//        val user = User(Lat, Lng)
//        database.child("users").child(userId.toString()).setValue(user)
//    }
//
//    fun updateUserWithOutTile(userId: Long, latitude: Double, longitude: Double) {
//        val user = User(Lat, Lng)
//        database.child("users").child(userId.toString()).setValue(user)
//    }
//    fun updateTile(userId: Long, tile: String){
//        database.child("users").child(userId.toString())
//    }

    //산책중일때
    fun updateUser(userId: Long, latitude: Double, longitude: Double, tile: String) {
        val user = UserDto(latitude, longitude, tile, true)
        Log.d(TAG, "updateUser : " + user.toString())
        database.child("users").child(userId.toString()).setValue(user)
        database.child("users").child(userId.toString()).child("timestamp")
            .setValue(ServerValue.TIMESTAMP)
//        database.child("users").child(userId.toString()).child("timestamp").setValue(Date())
    }

    fun endWalking(userId: Long) {
        database.child("users").child(userId.toString()).child("walking").setValue(false)
    }

    fun getValue(userId: String): LatLng {
        var a = HashMap<String, Any?>();
        database.child("users").child(userId).get().addOnSuccessListener {
            a = it.value as HashMap<String, Any?>
            Log.e("firebase", a.toString() + a?.javaClass?.name.toString())
            lat = a.get("lat") as Double
            lng = a.get("lng") as Double
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
        return LatLng(lat, lng)
    }

    //    fun getWalkingUser(tile:Array<String>, users:Map<Long,WalkingUserDto>){//참조형 변수를 이용해 값 변경이 가능하게
//        database.child("users").orderByChild("walking").equalTo(true).addValueEventListener(object:ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("firebase", "Error getting data" + error.toString())
//            }
//        })
//    }
    suspend fun getWalkingUser(
        tile: String,
    ): Map<Long, WalkingUserDto> {
        var map = HashMap<Long, WalkingUserDto>()
        var filterMap = HashMap<Long, WalkingUserDto>()
        //수정 필요


        database.child("users").get().addOnSuccessListener {
            if (it == null) {
                throw Exception("데이터가 없습니다.")
            }

            for (childDs in it.children) {

                var userId = childDs.key!!.toLong()
                var latitude = childDs.child("latitude").value as Double
                var longitude = childDs.child("longitude").value as Double
                var tile = childDs.child("tile").value as String
                var timestamp = childDs.child("timestamp").value.toString()
                var walking = childDs.child("walking").value as Boolean
                var dto = WalkingUserDto(userId, latitude, longitude, tile, walking)
                map.put(userId, dto)
            }

//            데이터 필터링
            for (userData in map) {
                if (tile == userData.value.tile) {
                    if (userData.value.walking) {
                        filterMap.put(userData.key, userData.value)
                    }
                }
            }
        }.addOnFailureListener {
            Log.d("firebase", "get all data error", it)
        }

        Log.d(TAG, "get user data end : " + filterMap.toString())


    Log.d(TAG, "out method : " + filterMap.toString())
    return filterMap

}


}


//@IgnoreExtraProperties
//data class User(val latitude: Double? = null, val longitude: Double? = null) {
//    // Null default values create a no-argument default constructor, which is needed
//    // for deserialization from a DataSnapshot.
//
//}

@IgnoreExtraProperties
data class UserDto(
    val latitude: Double?,
    val longitude: Double,
    val tile: String,
    val walking: Boolean
) {

    override fun toString(): String {
        return "UserDto(latitude=$latitude, longitude=$longitude, tile='$tile', walking=$walking')"
    }
}
