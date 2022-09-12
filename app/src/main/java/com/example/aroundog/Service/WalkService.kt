package com.example.aroundog.Service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface WalkService {
    @Multipart
    @POST("/walk/{userId}/add")
    @JvmSuppressWildcards
    fun addWalk(@Path("userId", encoded = true) userId:String, @PartMap params: HashMap<String, RequestBody>, @Part image:MultipartBody.Part):Call<Void>

}