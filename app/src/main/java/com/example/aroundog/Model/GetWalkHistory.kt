package com.example.aroundog.Model

import com.google.gson.annotations.SerializedName
import com.naver.maps.geometry.LatLng
import retrofit2.http.FormUrlEncoded
import java.lang.reflect.Constructor

data class GetWalkHistory (
    @SerializedName("success")
    val success:String,

    @SerializedName("serialNumber")
    val serialNumber:String,

    @SerializedName("user_id")
    val userId:String,

    @SerializedName("history")
    val history:String,

    @SerializedName("good")
    val good:Int,

    @SerializedName("bad")
    val bad:Int,

    @SerializedName("imgSrc")
    val imgSrc:String,

    @SerializedName("imgFile")
    val imgFile:String,
    )