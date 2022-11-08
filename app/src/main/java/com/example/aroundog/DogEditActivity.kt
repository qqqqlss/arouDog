package com.example.aroundog

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.MutableLiveData
import com.example.aroundog.Model.Gender
import com.example.aroundog.Service.DogService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.utils.DogBreedData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogEditActivity : AppCompatActivity() {
    val TAG = "DogEditActivity"
    var dogName: EditText? = null
    var dogAge: EditText? = null
    var dogHeight: EditText? = null
    var dogWeight: EditText? = null
    var dogGender: RadioGroup? = null
    var dogBreed: TextView? = null
    var dogCheck: TextView? = null
    var editButton: Button? = null
    lateinit var editDogBack:ImageButton

    companion object{
        val editDogInfo = MutableLiveData<DogDto>()
    }

    init {
        SelectDogActivity.selectDog.observe(this){
            dogBreed!!.text = DogBreedData.getBreed(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dog_edit)

        val dogInfo = intent.getSerializableExtra("info") as DogDto

        //뷰 찾기
        setView()

        //뷰에 정보 입력
        setDogInfo(dogInfo)

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        var retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(DogService::class.java)

        dogBreed!!.setOnClickListener {
            var intent = Intent(this, SelectDogActivity::class.java)
            intent.putExtra("breed", dogInfo.breed)
            it.context.startActivity(intent)
        }

        editButton!!.setOnClickListener {
            val strDogName = dogName!!.text.toString()
            val strDogAge = dogAge!!.text.toString()
            val strDogWeight = dogWeight!!.text.toString()
            val strDogHeight = dogHeight!!.text.toString()
            if (strDogName == "" || strDogAge == "" || strDogHeight == "" || strDogWeight == "" || DogBreedData.getId(dogBreed!!.text.toString()) == -1L) {
                dogCheck!!.visibility = View.VISIBLE
            } else {
                var gender: Gender
                if (dogGender!!.checkedRadioButtonId == R.id.editDogMan) {
                    gender = Gender.MAN
                } else {
                    gender = Gender.WOMAN
                }
                var dogDto = DogDto(
                    dogInfo.dogId,
                    strDogName,
                    strDogAge.toInt(),
                    strDogWeight.toDouble(),
                    strDogHeight.toDouble(),
                    gender,
                    DogBreedData.getId(dogBreed!!.text.toString()),//종을 해당 종 id로 변경
                    dogInfo.dogImgList
                )

                retrofit.updateDog(
                    dogDto.dogId,
                    dogDto.dogName,
                    dogDto.dogAge,
                    dogDto.dogWeight,
                    dogDto.dogHeight,
                    dogDto.dogGender,
                    dogDto.breed
                ).enqueue(object : Callback<Boolean> {
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful && response.body() == true) {
                            editDogInfo.postValue(dogDto)
                        }else{
                            Toast.makeText(applicationContext, "정보 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Toast.makeText(applicationContext, "정보 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "수정 실패", t)
                    }
                })
            }
            finish()
        }

        //뒤로가기 버튼 리스너
        editDogBack.setOnClickListener {
            finish()
        }

    }


    private fun setDogInfo(dogInfo: DogDto) {
        dogName!!.setText(dogInfo.dogName)
        dogAge!!.setText(dogInfo.dogAge.toString())
        dogHeight!!.setText(dogInfo.dogHeight.toString())
        dogWeight!!.setText(dogInfo.dogWeight.toString())
        if (dogInfo.dogGender == Gender.MAN) {
            dogGender!!.check(R.id.editDogMan)
        } else {
            dogGender!!.check(R.id.editDogWoman)
        }
        dogBreed!!.text = DogBreedData.getBreed(dogInfo.breed)
    }

    private fun setView() {
        dogName = findViewById(R.id.editDogName)
        dogAge = findViewById(R.id.editDogAge)
        dogHeight = findViewById(R.id.editDogHeight)
        dogWeight = findViewById(R.id.editDogWeight)
        dogGender = findViewById(R.id.editDogGender)
        dogBreed = findViewById(R.id.editDogBreed)
        dogCheck = findViewById(R.id.editDogCheck)
        editButton = findViewById(R.id.editDogUpdate)
        editDogBack = findViewById(R.id.editDogBack)
    }
}