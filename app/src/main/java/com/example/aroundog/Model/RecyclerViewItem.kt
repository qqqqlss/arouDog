package com.example.aroundog.Model

import android.graphics.Bitmap

class RecyclerViewItem(
    var loginUserId:String,
    var walkId:Long,
    var img:Bitmap,
    var userId:String,
    var good:Int,
    var bad:Int,
    var walkSecond:Long,
    var checkGood:Boolean,
    var checkBad:Boolean
    ) {


}