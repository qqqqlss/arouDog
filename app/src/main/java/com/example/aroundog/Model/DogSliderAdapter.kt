package com.example.aroundog.Model

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.R
import org.jetbrains.anko.find

class DogSliderAdapter(val imgList:List<Int>): RecyclerView.Adapter<DogSliderAdapter.ViewHolder>() {


    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var dogSlider:ImageView
        init {
            dogSlider = view.findViewById(R.id.dogSlider)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dog_slider, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dogSlider.setImageResource(imgList[position])
    }

    override fun getItemCount(): Int {
        return imgList.size
    }


}