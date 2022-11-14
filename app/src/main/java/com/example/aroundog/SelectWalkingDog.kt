package com.example.aroundog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.Model.DogSliderAdapter
import com.example.aroundog.Model.SelectWalkingDogAdapter
import com.example.aroundog.dto.DogDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class SelectWalkingDog(context: Context) : Dialog(context) {
    lateinit var selectWalkingDogRecyclerView:RecyclerView
    lateinit var selectWalkingDogButton:Button
    lateinit var selectBack:ImageButton

    companion object{
        var selectWalkingDog = MutableLiveData<MutableList<Long>>()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_walking_dog)

        setView()

        var dog_info_pref =
            context.getSharedPreferences("dogInfo", AppCompatActivity.MODE_PRIVATE)
        var makeGson = GsonBuilder().create()
        var type: TypeToken<MutableList<DogDto>> = object: TypeToken<MutableList<DogDto>>(){}
        //강아지 선택
        var listStr = dog_info_pref.getString("dogList", "")
        var dogList:MutableList<DogDto> = mutableListOf()//소유 강아지 리스트
        if (listStr != "") {
            dogList = makeGson.fromJson<MutableList<DogDto>>(listStr, type.type)
        }


        var selectDog = mutableListOf<Long>()

        var listener = object: SelectWalkingDogAdapter.ItemClickListener{
            override fun onItemClicked(view: View, position:Int, selectWalkingDogItemView:View) {
                //선택 효과
                if (selectDog.contains(dogList[position].dogId)) {//있을때
                    view.setBackgroundResource(0)
                    view.background = context.resources.getDrawable(R.drawable.walking_dog_not_select, null)
                    selectWalkingDogItemView.background = context.resources.getDrawable(R.color.lightGray, null)
                    selectDog.remove(dogList[position].dogId)
                } else {
                    view.background = context.resources.getDrawable(R.drawable.style_rounded_layout, null)
                    selectWalkingDogItemView.background = context.resources.getDrawable(R.color.brown, null)
                    //리스트에 추가
                    selectDog.add(dogList[position].dogId)
                }

            }
        }

        var mLayoutManager = LinearLayoutManager(context);
        var adapter = SelectWalkingDogAdapter(dogList)
        adapter.adapterListener = listener
        selectWalkingDogRecyclerView.layoutManager = mLayoutManager
        selectWalkingDogRecyclerView.adapter = adapter


        //뒤로가기 버튼
        selectBack.setOnClickListener {
            dismiss()
        }

        //선택완료 버튼
        selectWalkingDogButton.setOnClickListener {
            if (selectDog.isEmpty()) {
                selectDog.add(-1L)
            }
            selectWalkingDog.postValue(selectDog)
            dismiss()
        }

    }

    fun setView() {
        selectWalkingDogRecyclerView = findViewById(R.id.selectWalkingDogRecyclerView)
        selectWalkingDogButton = findViewById<Button>(R.id.selectWalkingDogButton)
        selectBack = findViewById<ImageButton>(R.id.selectWalkingDogBack)
    }
}