package com.example.aroundog.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.UserService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IdFindFragment : Fragment() {
    val TAG = "IDFINDFRAGMENT"
    lateinit var retrofit: UserService
    lateinit var findIdUserNameET: EditText
    lateinit var findIdEmailET: EditText
    lateinit var findIdCheckTV: TextView
    lateinit var findIdButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(UserService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        findIdButton.setOnClickListener {
            var userName = findIdUserNameET.text.toString()
            var email = findIdEmailET.text.toString()

            // 빈 값이 존재할 경우
            if (userName == "" || email == "") {
                findIdCheckTV!!.visibility = View.VISIBLE // 값을 채워넣으라는 안내문 출력
            } else { // 값을 모두 넣었을 때
                retrofit.findId(userName, email).enqueue(object : Callback<Boolean> {
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if (response.isSuccessful) { // 성공적으로 받아왔을 때
                            if (response.body() == true) {
                                Toast.makeText(context, "이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
                                activity!!.finish()
                                Log.d(TAG, "성공")
                            } else {
                                Toast.makeText(context, "입력하신 정보를 확인해주세요", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d(TAG, "실패")
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "오류가 발생했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Toast.makeText(context, "서버가 닫혀있습니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "서버 통신 에러")
                    }
                })
            }

        }
        return view
    }


    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_id_find, container, false) as ViewGroup

        findIdUserNameET = view.findViewById(R.id.findIdUserNameET)
        findIdEmailET = view.findViewById(R.id.findIdEmailET)
        findIdButton = view.findViewById(R.id.findIdButton)
        findIdCheckTV = view.findViewById(R.id.findIdCheckTV)
        return view
    }
}