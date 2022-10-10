package com.example.aroundog.dto

import com.example.aroundog.Model.Gender

data class UserAndDogDto(var userId:String, var password:String, var userAge:Int, var image:Int, var userName:String, var phone:String, var email:String, var userGender: Gender,
                            var dogId:Long, var dogName:String, var dogAge:Int, var dogWeight:Double, var dogHeight:Double, var dogGender: Gender, var breed:Long, var dogImgList:ArrayList<ImgDto>, var success:Boolean) {
    fun isSuccess():Boolean {
        return this.success;
    }

}