package com.example.aroundog.Service

import androidx.compose.ui.text.font.FontWeight
import com.example.aroundog.Model.Gender
import com.example.aroundog.dto.UpdateDogImageDto
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

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
    ): Call<Long>

    @DELETE("/dogImg/{dogImgId}")
    fun deleteDogImg(
        @Path("dogImgId") dogImgId:Long
    ):Call<Boolean>

    @DELETE("/dog/{dogId}")
    fun deleteDog(
        @Path("dogId") dogId:Long
    ):Call<Boolean>

    @Multipart
    @POST("/dogImg/{dogId}")
    fun updateDogImg(
        @Path("dogId") dogId: Long,
        @Part image:MultipartBody.Part
    ):Call<UpdateDogImageDto>
    
    @FormUrlEncoded
    @PATCH("/dog/{dogId}")
    fun updateDog(
        @Path("dogId")dogId:Long,
        @Field("name") dogName:String,
        @Field("age") dogAge:Int,
        @Field("weight") dogWeight:Double,
        @Field("height") dogHeight:Double,
        @Field("gender") dogGender:Gender,
        @Field("breed") breed:Long
    ):Call<Boolean>
}