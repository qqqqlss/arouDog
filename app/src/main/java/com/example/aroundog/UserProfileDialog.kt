package com.example.aroundog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.cardview.widget.CardView

// 유저 프로필 설정 다이얼로그


// 유저 프로필 설정 다이얼로그
class UserProfileDialog(context: Context, private val select: Select, profile: ImageView) :
    Dialog(context) {
    var BlueProfile: CardView? = null
    var GreenProfile: CardView? = null
    var PinkProfile: CardView? = null
    /*
    var BlueMale: CardView? = null
    var PurpleFemale: CardView? = null
    var PurpleMale: CardView? = null
     */
    var selectImage = 1

    interface Select {
        fun clickProfile(userImg: Int)
    }

    fun selectProfile(profile: ImageView) {
        BlueProfile = findViewById(R.id.profile1)
        GreenProfile = findViewById(R.id.profile2)
        PinkProfile = findViewById(R.id.profile3)
        /*
        BlueMale = findViewById(R.id.dialog_user_image_blue_male)
        PurpleFemale = findViewById(R.id.dialog_user_image_purple_female)
        PurpleMale = findViewById(R.id.dialog_user_image_purple_male)
        */

        BlueProfile!!.setOnClickListener(View.OnClickListener {
            selectImage = 1
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.profile_1)
            dismiss()
        })
        GreenProfile!!.setOnClickListener(View.OnClickListener {
            selectImage = 2
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.profile_2)
            dismiss()
        })
        PinkProfile!!.setOnClickListener(View.OnClickListener {
            selectImage = 3
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.profile_3)
            dismiss()
        })
        /*
        BlueMale!!.setOnClickListener(View.OnClickListener {
            selectImage = 4
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.ic_profile_male_blue)
            dismiss()
        })
        PurpleFemale!!.setOnClickListener(View.OnClickListener {
            selectImage = 5
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.ic_profile_female_purple)
            dismiss()
        })
        PurpleMale!!.setOnClickListener(View.OnClickListener {
            selectImage = 6
            select.clickProfile(selectImage)
            profile.setImageResource(R.drawable.ic_profile_male_purple)
            dismiss()
        })
         */
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 없애기
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경화면으로
        setContentView(R.layout.dialog_user_image)
        selectProfile(profile)
    }
}