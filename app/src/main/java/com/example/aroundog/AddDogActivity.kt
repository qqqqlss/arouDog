package com.example.aroundog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.MutableLiveData
import com.example.aroundog.Model.Gender
import com.example.aroundog.Service.DogService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.ImgDto
import com.example.aroundog.utils.DogBreedData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddDogActivity : AppCompatActivity() {
    var dog_name: EditText? = null
    var dog_age: EditText? = null
    var dog_height: EditText? = null
    var dog_weight: EditText? = null
    var dogGender: RadioGroup? = null
    var dogBreed: TextView? = null
    var dogCheck: TextView? = null
    var dogButton: Button? = null

    lateinit var addDogBack:ImageButton
    lateinit var retrofit: DogService
    var userId = ""
    var TAG = "ADDDOGACTIVITY"

    init {
        SelectDogActivity.selectDog.observe(this){
            dogBreed!!.text = DogBreedData.getBreed(it)
        }
    }

    companion object{
        val newDogData = MutableLiveData<DogDto>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dog)
        setView()

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(DogService::class.java)

        var user_info_pref =
            getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id", "error").toString()

        //뒤로가기 버튼
        addDogBack.setOnClickListener {
            finish()
        }

        dogBreed!!.setOnClickListener {
            var intent = Intent(this, SelectDogActivity::class.java)
            intent.putExtra("breed",-1L)
            it.context.startActivity(intent)
        }

        dogButton!!.setOnClickListener(View.OnClickListener {
            val dogName = dog_name!!.text.toString()
            val dogAge = dog_age!!.text.toString()
            val dogHeight = dog_height!!.text.toString()
            val dogWeight = dog_weight!!.text.toString()

            if (dogName == "" || dogAge == "" || dogHeight == "" || dogWeight == "" || dogGender!!.checkedRadioButtonId == -1) {
                dogCheck!!.visibility = View.VISIBLE
                return@OnClickListener
            } else {
                var strGender = if (dogGender!!.checkedRadioButtonId == R.id.dog_man) {
                    "MAN"
                } else {
                    "WOMAN"
                }

                retrofit.addDog(
                    userId,
                    DogBreedData.getId(dogBreed!!.text.toString()),//종을 해당 종 id로 변경
                    dogName,
                    dogAge.toInt(),
                    dogWeight.toDouble(),
                    dogHeight.toDouble(),
                    strGender
                ).enqueue(object :
                    Callback<Long> {
                    override fun onResponse(call: Call<Long>, response: Response<Long>) {
                        if (response.isSuccessful) {
                            if (response.body() != -100L) { //실패시 -100
                                var dogId:Long = response.body()!!
                                val newDog = DogDto(
                                    dogId,
                                    dogName,
                                    dogAge.toInt(),
                                    dogWeight.toDouble(),
                                    dogHeight.toDouble(),
                                    Gender.valueOf(strGender),
                                    DogBreedData.getId(dogBreed!!.text.toString()),
                                    mutableListOf<ImgDto>()
                                )
                                newDogData.postValue(newDog)//newDogData 업데이트 알림
                                Log.d(TAG, "성공")
                                Toast.makeText(applicationContext, "추가 성공", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            } else {
                                Log.d(TAG, "실패")
                                Toast.makeText(applicationContext, "추가 실패", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            }

                        }
                    }

                    override fun onFailure(call: Call<Long>, t: Throwable) {
                        Log.d(TAG, "추가 실패 $t")
                        Toast.makeText(applicationContext, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                })
            }
        })
    }

    override fun onBackPressed() {
        Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
        return
    }

    private fun setView() {
        dog_name = findViewById(R.id.dog_name)
        dog_age = findViewById(R.id.dog_age)
        dog_height = findViewById(R.id.dog_height)
        dog_weight = findViewById(R.id.dog_weight)
        dogGender = findViewById(R.id.dog_gender)
        dogBreed = findViewById(R.id.dog_breed)
        dogCheck = findViewById(R.id.dog_check)
        dogButton = findViewById(R.id.dog_button)
        addDogBack = findViewById(R.id.addDogBack)
    }
}