package com.example.aroundog

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.aroundog.fragments.ProgressDialogFragment

class BaseApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        instance=this
    }
    fun progressOn(view: Activity?) {
        if (view == null || view.isFinishing)
            return
        if (dialog!=null) {

        }

        if (dialog == null || (dialog != null && dialog!!.isShowing)) {
            dialog = AppCompatDialog(view).apply {
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dog_loading)
                show()
            }
        }
        else if (dialog!=null) {
            dialog!!.show()
            return
        }

        Glide.with(view).load(R.raw.dog_loading)
            .apply(RequestOptions().override(200, 200))
            .into(dialog!!.findViewById<ImageView>(R.id.load_image_view) as ImageView)
    }

    fun progressOff(){
        if(dialog!=null && dialog!!.isShowing)
            dialog!!.dismiss()
    }

    fun progressOnInFragment(fragment: Fragment?){
        if(fragment==null || fragment.isDetached)
            return

        if(fragmentDialog==null || (fragmentDialog!=null && !fragmentDialog!!.isVisible)) {
            fragmentDialog = ProgressDialogFragment()
            fragmentDialog!!.show(fragment.childFragmentManager,"PROGRESS")
        }
    }


    fun progressOffInFragment(){
        if(fragmentDialog!=null) {
            fragmentDialog!!.dismiss()
        }
    }

    companion object {
        lateinit var instance : BaseApplication
            private set //Only BaseApplication set the instance value
        var dialog: AppCompatDialog? = null
        var fragmentDialog: ProgressDialogFragment? = null
    }

}