package com.example.aroundog.Service

import androidx.compose.ui.text.font.FontWeight
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface DogService {

    @FormUrlEncoded
    @POST("/dog")
    fun addDog(
        @Field("userId") userId:String,
        @Field("dogId") dogId:Long,
        @Field("name") name:String,
        @Field("age") age:Int,
        @Field("weight") weight: Double,
        @Field("height") height:Double,
        @Field("gender") gender:String
    ): Call<Boolean>

    @DELETE("/dog/{dogImgId}")
    fun deleteDogImg(
        @Path("dogImgId") dogImgId:Long
    ):Call<Boolean>
}