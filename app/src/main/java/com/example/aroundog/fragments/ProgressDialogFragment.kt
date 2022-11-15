package com.example.aroundog.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.aroundog.R

class ProgressDialogFragment : DialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(activity)
        dialog.apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dog_loading)
        }
        Glide.with(activity!!).load(R.raw.dog_loading)
            .apply(RequestOptions().override(200,200))
            .into(dialog.findViewById<ImageView>(R.id.load_image_view) as ImageView)
        return dialog
    }
}