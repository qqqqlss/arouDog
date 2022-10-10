package com.example.aroundog.Model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.R
import com.example.aroundog.dto.ImgDto
import org.jetbrains.anko.find

class DogSliderAdapter(val imgList: List<ImgDto>): RecyclerView.Adapter<DogSliderAdapter.ViewHolder>() {
    val TAG = "DOGSLIDERADAPTER"


    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var dogSlider:ImageView
        var view = view
        var path: String = ""
        init {
            dogSlider = view.findViewById(R.id.dogSlider)
            dogSlider.setOnClickListener {
                Toast.makeText(view.context, "${path}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dog_slider, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.path = imgList[position].path

        var bitmap: Bitmap
        if (imgList[position].path == "error") {//비어있는경우
            holder.dogSlider.setImageResource(R.drawable.error2)
        } else {
            var byteArray: ByteArray = Base64.decode(imgList[position].img, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            Log.d(TAG, "itemList is not null")
            Log.d(TAG, "${imgList}")
            holder.dogSlider.setImageBitmap(bitmap)
        }

    }

    override fun getItemCount(): Int {
        return imgList.size
    }


}