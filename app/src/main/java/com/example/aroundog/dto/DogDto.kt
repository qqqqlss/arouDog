package com.example.aroundog.dto

import com.example.aroundog.Model.Gender

data class DogDto(var dogId:Long, var dogName:String, var dogAge:Int, var dogWeight:Double, var dogHeight:Double, var dogGender: Gender, var breed:Long):java.io.Serializable{

}
