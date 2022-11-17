package com.example.aroundog.dto

import java.time.LocalDateTime

data class WalkListDto(
    var walkId:Long,
    var good: Int,
    var bad: Int,
    var img: String,
    var checkGood:Boolean,
    var checkBad:Boolean,
    var second: Long,
    var distance: Long,
    var address:String
) {
}