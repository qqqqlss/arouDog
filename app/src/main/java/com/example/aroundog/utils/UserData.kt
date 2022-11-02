package com.example.aroundog.utils

import com.example.aroundog.Model.Gender

data class UserData(var id:String, var password:String, var userAge:Int, var image:Int, var userName:String, var phone:String, var email:String, var userGender: Gender):java.io.Serializable
