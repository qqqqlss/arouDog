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

//    <?php
//    $con = mysqli_connect("122.32.165.55", "aroundog", "abcd1234", "team");
//    mysqli_query($con,'SET NAMES utf8');
//
//    $id= $_POST["user_id"];
//    $history = $_POST["history"];
//
//    $statement = mysqli_prepare($con, "INSERT INTO Walk(user_id, history) VALUES (?,?)");
//    mysqli_stmt_bind_param($statement, "ss", $id, $history);
//    $exec = mysqli_stmt_execute($statement);
//
//    $response = array();
//
//    if($exec == false){
//        $response["success"] = "false";
//
//    }else{
//        $response["success"] = "true";
//
//    }
//
//    echo json_encode($response);
//    ?>



    @GET("/test.php")
    fun test(
    ):Call<JSONObject>
}