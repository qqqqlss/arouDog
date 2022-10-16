package com.example.aroundog.Model

import android.app.Activity
import android.app.AlertDialog
import android.app.LauncherActivity.ListItem
import android.content.ClipData.Item
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.AddDogActivity
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.DogService
import com.example.aroundog.dto.ImgDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogSliderAdapter(var imgList: MutableList<ImgDto>): RecyclerView.Adapter<DogSliderAdapter.ViewHolder>() {

    val TAG = "DOGSLIDERADAPTER"
    lateinit var adapterListener:ItemClickListener

    interface ItemClickListener{
        fun onItemClicked(view: View, position: Int)
    }
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var dogSlider:ImageView
        var view = view
        var path: String = ""
        var dogImgId:Long = 0

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        var retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(DogService::class.java)

        //ViewHolder에 어댑터 등록(아이템 삭제하기 위해)
        lateinit var adapter:DogSliderAdapter
        private val DEFAULT_GALLERY_REQUEST_CODE = 0
        fun linkAdapter(adapter: DogSliderAdapter):ViewHolder{
            this.adapter = adapter
            return this
        }

        init {
            dogSlider = view.findViewById(R.id.dogSlider)

            dogSlider.setOnLongClickListener {
                //삭제 예 아니오 다이얼로그
                if (dogImgId != -100L && dogImgId != -200L) {
                    //강아지 사진 길게 누를 시 삭제
                    val builder = AlertDialog.Builder(view.context)
                    builder.setTitle("사진 삭제")
                        .setMessage(
                            "사진을 삭제하시겠어요?"
                        )
                        .setPositiveButton("삭제", object:DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                retrofit.deleteDogImg(dogImgId).enqueue(object:Callback<Boolean>{
                                    override fun onResponse(
                                        call: Call<Boolean>,
                                        response: Response<Boolean>
                                    ) {
                                        if (response.isSuccessful) {
                                            if (response.body() == true) {
                                                adapter.imgList.removeAt(adapterPosition)
                                                adapter.notifyItemRemoved(adapterPosition)

                                                Toast.makeText(view.context, "삭제 완료", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(view.context, "삭제 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                        Toast.makeText(view.context, "삭제 실패", Toast.LENGTH_SHORT).show()
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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.dog_slider, parent, false)
        return ViewHolder(view).linkAdapter(this)//뷰홀더에 어댑터를 동록한 뒤 뷰홀더 리턴
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.path = imgList[position].path
        holder.dogImgId = imgList[position].id

        var bitmap: Bitmap
        if (imgList[position].path == "emptyImg") {//이미지가 없는 경우
            holder.dogSlider.setImageResource(R.drawable.dog_camera)
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

        //원클릭 이벤트 리스너
        holder.dogSlider.setOnClickListener {
                //path에 따라 리스너 달라지게
                //이미지 추가 이미지
                if (imgList[position].id == -100L) {
                    if(adapterListener != null) {
                        //adapterListener사용
                        adapterListener!!.onItemClicked(it, position)
                    }
                }

                //강아지 추가 이미지
                if (imgList[position].id==-200L) {
                    val intent = Intent(it.context, AddDogActivity::class.java)
                    it.context.startActivity(intent)
                }

                Toast.makeText(it.context, "path : ${imgList[position].path}, id : ${imgList[position].id}", Toast.LENGTH_SHORT).show()
            }

    }

    override fun getItemCount(): Int {
        return imgList.size
    }


}