package com.example.aroundog.Model

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.WalkInfoActivity
import com.example.aroundog.dto.UserAndDogDto
import com.example.aroundog.dto.UserCoordinateDogDto
import com.example.aroundog.utils.DogBreedData

class BottomSheetAdapter(var items:ArrayList<UserCoordinateDogDto>) : RecyclerView.Adapter<BottomSheetAdapter.ItemViewHolder>() {

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(item: UserCoordinateDogDto) {
            var bottomImgae = view.findViewById<ImageView>(R.id.bottomImage)
            var bottomName = view.findViewById<TextView>(R.id.bottomName)
            var bottomBreed = view.findViewById<TextView>(R.id.bottomBreed)
            var bottomAge = view.findViewById<TextView>(R.id.bottomAge)
            var bottomWeight = view.findViewById<TextView>(R.id.bottomWeight)
            var bottomGender = view.findViewById<TextView>(R.id.bottomGender)


            var path = BuildConfig.SERVER+"image/" + item.dogId
            Glide.with(view.context).load(path).error(R.drawable.error).into(bottomImgae)
            bottomName.text = item.dogName
            bottomBreed.text = "종 : " + item.dogBreed.kor
            bottomAge.text = "나이 : " + item.dogAge.toString() + "살"
            bottomWeight.text = "무게 : " + item.dogWeight.toString() + "kg"
            bottomGender.text = "성별 : " + item.dogGender.toString()


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_dialog_item, parent, false)
        return BottomSheetAdapter.ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var item = items[position]

        holder.bind(item)

    }

    override fun getItemCount(): Int {
        if(!items.isEmpty())
            return items.size
        else
            return 0
    }

}