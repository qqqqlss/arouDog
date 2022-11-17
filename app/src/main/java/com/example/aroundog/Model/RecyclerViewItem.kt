package com.example.aroundog.Model

import android.graphics.Bitmap

class RecyclerViewItem(
    var loginUserId:String,
    var walkId:Long,
    var img:Bitmap,
    var good:Int,
    var bad:Int,
    var checkGood:Boolean,
    var checkBad:Boolean,
    var second: Long,
    var distance: Long,
    var address:String

    ) {


}