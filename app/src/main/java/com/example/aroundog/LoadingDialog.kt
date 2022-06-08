package com.example.aroundog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window

// 액티비티, 프래그먼트 전 영역에 사용되는 로딩 다이얼로그입니다.
class LoadingDialog(context: Context, i: Int) : Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 없애기
        // setCanceledOnTouchOutside(false); // 외부 화면 터치해도 다이얼로그가 종료되지 않게
        setCancelable(false) // 외부 화면 터치, 뒤로가기를 눌러도 다어얼로그 종료 X
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경화면으로
        setContentView(R.layout.dialog_loading)
    }
}