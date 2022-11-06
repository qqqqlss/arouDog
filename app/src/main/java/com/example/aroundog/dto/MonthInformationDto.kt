package com.example.aroundog.dto

import java.time.LocalDateTime

data class MonthInformationDto(var walkId:Long, var startTime:LocalDateTime, var endTime:LocalDateTime, var second:Long, var distance:Long)
