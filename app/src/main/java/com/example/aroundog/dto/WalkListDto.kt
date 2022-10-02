package com.example.aroundog.dto

import java.time.LocalDateTime

data class WalkListDto(
    var walkId:Long,
    var userId: String,
    var courseCenter: String,
    var good: Int,
    var bad: Int,
    var img: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var checkGood:Boolean,
    var checkBad:Boolean,
    var walkSecond: Long
) {
}