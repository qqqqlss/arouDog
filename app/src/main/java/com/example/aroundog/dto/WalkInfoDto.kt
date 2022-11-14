package com.example.aroundog.dto

import java.time.LocalDateTime

data class WalkInfoDto(var walkId:Long, var course:String, var img:String, var startTime:LocalDateTime, var endTime: LocalDateTime, var second:Long, var distance:Long, var dogIds:String, var idNameMap:Map<Long, String>)
