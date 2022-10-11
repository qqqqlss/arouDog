package com.example.aroundog.Model

import android.content.Intent
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
import com.example.aroundog.AddDogActivity
import com.example.aroundog.R
import com.example.aroundog.dto.ImgDto

class DogSliderAdapter(val imgList: List<ImgDto>): RecyclerView.Adapter<DogSliderAdapter.ViewHolder>() {
    val TAG = "DOGSLIDERADAPTER"


    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var dogSlider:ImageView
        var view = view
        var path: String = ""
        var id:Long = 0

        init {
            dogSlider = view.findViewById(R.id.dogSlider)
            dogSlider.setOnClickListener {
                //path에 따라 리스너 달라지게
                //emptyImg시 이미지 추가 화면
                //emptyDog시 강아지 추가 화면
                if (id==-200L) {
                    val intent = Intent(view.context, AddDogActivity::class.java)
                    view.context.startActivity(intent)
                }

                Toast.makeText(view.context, "path : ${path}, id : $id", Toast.LENGTH_SHORT).show()
            }
            dogSlider.setOnLongClickListener {
                //삭제 예 아니오 다이얼로그


                //false일 경우 길게 누르고있을때 onlongclicklistiner, 손 뗄때 onclick발생
                //true일 경우 longclick만 발생
                return@setOnLongClickListener (true)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dog_slider, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.path = imgList[position].path
        holder.id = imgList[position].id

        var bitmap: Bitmap
        if (imgList[position].path == "emptyImg") {//이미지가 없는 경우
            holder.dogSlider.setImageResource(R.drawable.error2)
        }
        else if(imgList[position].path == "emptyDog"){//강아지가 없는 경우
            holder.dogSlider.setImageResource(R.drawable.add_dog)
        }
        else {
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