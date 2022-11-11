package com.example.aroundog.Model

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatDrawableManager.preload
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.aroundog.AddDogActivity
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.DogService
import com.example.aroundog.dto.DogIdImgIdFilename
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogSliderAdapter(var imgList: MutableList<DogIdImgIdFilename>): RecyclerView.Adapter<DogSliderAdapter.ViewHolder>() {

    val TAG = "DOGSLIDERADAPTER"
    lateinit var adapterListener:ItemClickListener

    var gsonInstance: Gson = GsonBuilder().setLenient().create()
    var retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER)
        .addConverterFactory(GsonConverterFactory.create(gsonInstance))
        .build()
        .create(DogService::class.java)
    interface ItemClickListener{
        fun onItemClicked(view: View, position: Int)
    }
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val TAG = "DOGSLIDERADAPTER"
        var dogSlider:ImageView
        var view = view

//        //ViewHolder에 어댑터 등록(아이템 삭제하기 위해)
        lateinit var adapter:DogSliderAdapter
        fun linkAdapter(adapter: DogSliderAdapter):ViewHolder{
            this.adapter = adapter
            return this
        }

        init {
            dogSlider = view.findViewById(R.id.dogSlider)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.dog_slider, parent, false)
        return ViewHolder(view).linkAdapter(this)//뷰홀더에 어댑터를 동록한 뒤 뷰홀더 리턴
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var view = holder.view
        var dogSlider = holder.dogSlider
        var dogIdImgIdFilename = imgList[position]
        var dogId = dogIdImgIdFilename.dogId
        var filename = dogIdImgIdFilename.filename
        var imgId = dogIdImgIdFilename.imgId

        var path = BuildConfig.SERVER + "image/" + dogId + "/" + filename

        if (filename == "emptyImg") {//이미지가 없는 경우
            dogSlider.setImageResource(R.drawable.dog_camera)
        }
        else if(filename == "emptyDog"){//강아지가 없는 경우
            dogSlider.setImageResource(R.drawable.add_dog)
        }
        else {//강아지 사진 추가
            //gif 를 사용하기 위해 plcaceholder대신 thumbnail 사용
            Glide.with(view.context).load(path).thumbnail(Glide.with(view.context).load(R.drawable.loading)).listener(object:RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    dogSlider.setImageResource(R.drawable.error)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false;
                }

            }).into(dogSlider)
        }

        //원클릭 이벤트 리스너
        dogSlider.setOnClickListener {
            //path에 따라 리스너 달라지게
            //강아지 사진 추가 이미지
            if (imgList[holder.adapterPosition].imgId == -100L) {
                if(adapterListener != null) {
//                        adapterListener사용
                    adapterListener!!.onItemClicked(it, holder.adapterPosition)
                }
            }

            //강아지 추가 이미지
            if (dogIdImgIdFilename.imgId==-200L) {
                val intent = Intent(it.context, AddDogActivity::class.java)
                it.context.startActivity(intent)
            }

            Toast.makeText(it.context, "filename : $filename, dogId : $dogId, imgId : $imgId", Toast.LENGTH_SHORT).show()
        }

        dogSlider.setOnLongClickListener {
            //리스트뷰에서 지우면 포지션인 -1이 되므로 미리 저장
            var selectPosition = holder.adapterPosition
            //삭제 예 아니오 다이얼로그
            if (imgList[selectPosition].imgId != -100L && imgList[selectPosition].imgId != -200L) {
                //강아지 사진 길게 누를 시 삭제
                val builder = AlertDialog.Builder(it.context)
                builder.setTitle("사진 삭제")
                    .setMessage(
                        "사진을 삭제하시겠어요?"
                    )
                    .setPositiveButton("삭제", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                            retrofit.deleteDogImg(imgId)
                                .enqueue(object : Callback<Boolean> {
                                    override fun onResponse(
                                        call: Call<Boolean>,
                                        response: Response<Boolean>
                                    ) {
                                        if (response.isSuccessful) {
                                            if (response.body() == true) {
                                                notifyItemRemoved(selectPosition)
                                                notifyItemRangeChanged(selectPosition, imgList.size)
                                                imgList.removeAt(selectPosition)

                                                Toast.makeText(
                                                    it.context,
                                                    "삭제 완료",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                Log.d(TAG, "imgList size : ${imgList.size}")
                                                if (imgList.size == 0) {
                                                    imgList.add(
                                                        DogIdImgIdFilename(
                                                            dogId,
                                                            -100,
                                                            "emptyImg"
                                                        )
                                                    )
                                                    notifyItemInserted(0)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    it.context,
                                                    "삭제 실패",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                        Toast.makeText(it.context, "삭제 실패", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                })
                        }
                    })
                builder.setNegativeButton("취소") { dialog, i ->
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }

            //false일 경우 길게 누르고있을때 onlongclicklistiner, 손 뗄때 onclick발생
            //true일 경우 longclick만 발생
            return@setOnLongClickListener (true)
        }

        //미리 로딩
        if (position <= imgList.size -1) {
            //size -1은 addimg
            val endPosition = if (position + 2 > imgList.size-1) {
                imgList.size-1
            } else {
                position + 2
            }
            imgList.subList(position, endPosition ).map { it }.forEach {
                Glide.with(view.context).load(BuildConfig.SERVER + "image/" + it.dogId + "/" + it.filename).preload()
            }
        }
    }

    override fun getItemCount(): Int {
        return imgList.size
    }


}