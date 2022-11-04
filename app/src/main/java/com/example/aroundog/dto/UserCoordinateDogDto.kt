package com.example.aroundog.dto

import com.example.aroundog.Model.DogBreed
import com.example.aroundog.Model.Gender

data class UserCoordinateDogDto(var userId:String, var userName:String, var userAge: Integer, var userImage:Int, var latitude:Double, var longitude:Double, var dogId:Long,
                                var dogName:String, var dogAge:Integer, var dogGender: Gender, var dogWeight:Double, var dogBreed: DogBreed
) {
}