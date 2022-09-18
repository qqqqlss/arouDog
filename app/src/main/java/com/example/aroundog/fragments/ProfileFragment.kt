package com.example.aroundog.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.DogService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ProfileFragment : Fragment() {
    var dog_name: EditText? = null
    var dog_age: EditText? = null
    var dog_height: EditText? = null
    var dog_weight: EditText? = null
    var dogGender: RadioGroup? = null
    var dogBreed: RadioGroup? = null
    var dogCheck: TextView? = null
    var dogButton: Button? = null
    lateinit var retrofit:DogService
    var userId = ""
    var TAG = "PROFILEFRAGMENT"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(DogService::class.java)

        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id", "error").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)
        // Inflate the layout for this fragment
        dogButton!!.setOnClickListener(View.OnClickListener {
            val dogName = dog_name!!.text.toString()
            val dogAge = dog_age!!.text.toString()
            val dogHeight = dog_height!!.text.toString()
            val dogWeight = dog_weight!!.text.toString()

            if (dogName == "" || dogAge == "" || dogHeight == "" || dogWeight == "" || dogGender!!.checkedRadioButtonId ==-1 || dogBreed!!.checkedRadioButtonId == -1) {
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
                } else if(dogBreed!!.checkedRadioButtonId == R.id.dog_medium){
                    2L
                } else{
                    3L
                }

                retrofit.addDog(userId, intBreed, dogName, dogAge.toInt(), dogWeight.toDouble(), dogHeight.toDouble(), strGender).enqueue(object:Callback<Boolean>{
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful) {
                            if (response.body() == true) {
                                Log.d(TAG, "성공")
                                Toast.makeText(context, "추가 성공", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.d(TAG, "실패")
                                Toast.makeText(context, "추가 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Log.d(TAG, "추가 실패 $t")
                        Toast.makeText(context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                })

                dog_name!!.text= null
                dog_age!!.text = null
                dog_height!!.text=null
                dog_weight!!.text = null
                dogCheck!!.visibility = View.GONE
                dogGender!!.clearCheck()
                dogBreed!!.clearCheck()

            }
        })
        return view
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_add_dog, container, false) as ViewGroup
        dog_name = view.findViewById(R.id.dog_name)
        dog_age = view.findViewById(R.id.dog_age)
        dog_height = view.findViewById(R.id.dog_height)
        dog_weight = view.findViewById(R.id.dog_weight)
        dogGender = view.findViewById(R.id.dog_gender)
        dogBreed = view.findViewById(R.id.dog_breed)
        dogCheck = view.findViewById(R.id.dog_check)
        dogButton = view.findViewById(R.id.dog_button)
        return view
    }
}