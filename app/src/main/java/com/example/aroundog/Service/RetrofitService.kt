package com.example.aroundog.Service

import com.example.aroundog.Model.GetWalkHistory
import com.example.aroundog.Model.UpdateWalkHistory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

//    @Multipart
//    @POST("/updateTest.php")
//    fun postWalkHistory(
//        @PartMap user_id:RequestBody,
//        @Part history:RequestBody,
//        @Part image:MultipartBody.Part
//    ): Call<UpdateWalkHistory>

    @Multipart
    @POST("/updateHistory.php")
    fun postWalkHistory(
        @Part("user_id") user_id:RequestBody,
        @Part("history") history: RequestBody,
        @Part image:MultipartBody.Part
    ): Call<UpdateWalkHistory>

    @GET("/getHistory.php")
    fun getWalkHistory(

    ):Call<ArrayList<GetWalkHistory>>

    @FormUrlEncoded
    @POST("/clickButton.php")
    fun clickButton(
        @Field("button") button:String,
        @Field("serialNumber") serialNumber:String
    ):Call<UpdateWalkHistory>

    @GET("/test.php")
    fun test(
    ):Call<JSONObject>
}