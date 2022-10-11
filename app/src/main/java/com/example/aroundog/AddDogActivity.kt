package com.example.aroundog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.aroundog.Service.DogService
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
    var dogBreed: RadioGroup? = null
    var dogCheck: TextView? = null
    var dogButton: Button? = null
    lateinit var retrofit: DogService
    var userId = ""
    var TAG = "ADDDOGACTIVITY"
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

        dogButton!!.setOnClickListener(View.OnClickListener {
            val dogName = dog_name!!.text.toString()
            val dogAge = dog_age!!.text.toString()
            val dogHeight = dog_height!!.text.toString()
            val dogWeight = dog_weight!!.text.toString()

            if (dogName == "" || dogAge == "" || dogHeight == "" || dogWeight == "" || dogGender!!.checkedRadioButtonId == -1 || dogBreed!!.checkedRadioButtonId == -1) {
                dogCheck!!.visibility = View.VISIBLE
                return@OnClickListener
            } else {
                var strGender = if (dogGender!!.checkedRadioButtonId == R.id.dog_man) {
                    "MAN"
                } else {
                    "WOMAN"
                }

                var intBreed = if (dogBreed!!.checkedRadioButtonId == R.id.dog_big) {
                    1L
                } else if (dogBreed!!.checkedRadioButtonId == R.id.dog_medium) {
                    2L
                } else {
                    3L
                }

                retrofit.addDog(
                    userId,
                    intBreed,
                    dogName,
                    dogAge.toInt(),
                    dogWeight.toDouble(),
                    dogHeight.toDouble(),
                    strGender
                ).enqueue(object :
                    Callback<Boolean> {
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful) {
                            if (response.body() == true) {
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

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
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
    }
}