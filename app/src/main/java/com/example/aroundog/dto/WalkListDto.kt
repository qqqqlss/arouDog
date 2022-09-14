package com.example.aroundog.dto

import java.time.LocalDateTime

data class WalkListDto(
    var id:Long,
    var userId: String,
    var courseCenter: String,
    var good: Int,
    var bad: Int,
    var img: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var walkSecond: Long
) {
}