package com.example.aroundog.Model

import android.graphics.Bitmap

class RecyclerViewItem(
    var id:Long,
    var img:Bitmap,
    var userId:String,
    var good:Int,
    var bad:Int,
    var walkSecond:Long
    ) {


}