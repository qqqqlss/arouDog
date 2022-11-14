package com.example.aroundog.Model

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.dto.DogDto
import com.example.aroundog.utils.DogBreedData

class SelectWalkingDogAdapter(var dogList:MutableList<DogDto>): RecyclerView.Adapter<SelectWalkingDogAdapter.ViewHolder2>(){
    interface ItemClickListener{
        fun onItemClicked(view: View, position: Int, selectWalkingDogItemView:View)
    }


    lateinit var adapterListener:ItemClickListener

    class ViewHolder2(view: View):RecyclerView.ViewHolder(view){
        var view = view
        lateinit var selectWalkingDogItemView:View

        //ViewHolder에 어댑터 등록(아이템 삭제하기 위해)
        lateinit var adapter:SelectWalkingDogAdapter
        fun linkAdapter(adapter: SelectWalkingDogAdapter): SelectWalkingDogAdapter.ViewHolder2 {
            this.adapter = adapter
            return this
        }

        fun bind(dog: DogDto) {
            var imageView = view.findViewById<ImageView>(R.id.selectWalkingDogItemImageView)
            var nameTV = view.findViewById<TextView>(R.id.selectWalkingDogItemName)
            var breedTV = view.findViewById<TextView>(R.id.selectWalkingDogItemBreed)
            var ageTV = view.findViewById<TextView>(R.id.selectWalkingDogItemAge)
            var weightTV = view.findViewById<TextView>(R.id.selectWalkingDogItemDogWeight)
            selectWalkingDogItemView = view.findViewById(R.id.selectWalkingDogItemView)

            var dogId = dog.dogId
            var filename = dog.dogImgList[0].fileName
            var path = BuildConfig.SERVER+"image/" + dogId + "/"+filename
            Glide.with(view.context).load(path).override(100).error(R.drawable.error).into(imageView)

            nameTV.text = dog.dogName
            breedTV.text = "종 : " + DogBreedData.getBreed(dog.breed)
            ageTV.text = "나이 : " + dog.dogAge.toString()
            weightTV.text = "키 : " + dog.dogWeight.toString()

            Log.d("sex", "sex")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder2 {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.select_walking_dog_item, parent, false)
        return SelectWalkingDogAdapter.ViewHolder2(view).linkAdapter(this)//뷰홀더에 어댑터를 동록한 뒤 뷰홀더 리턴
    }

    override fun onBindViewHolder(holder: ViewHolder2, position: Int) {
        var dog = dogList[position]
        holder.bind(dog)
        holder.view.setOnClickListener {
            if(adapterListener != null) {
                adapterListener!!.onItemClicked(it, holder.adapterPosition, holder.selectWalkingDogItemView)
            }
        }
    }

    override fun getItemCount(): Int {
        return dogList.size
    }
}