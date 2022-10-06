package com.example.aroundog.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aroundog.BuildConfig
import com.example.aroundog.R
import com.example.aroundog.Service.IntroService
import com.example.aroundog.Service.UserService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PwFindFragment : Fragment() {
    val TAG = "PWFINDFRAGMENT"
    lateinit var retrofit: UserService
    lateinit var validateRetrofit: IntroService
    lateinit var findPwUserIdET: EditText
    lateinit var findPwValidateButton: ImageButton
    lateinit var findPwUserIdNullTV: TextView
    lateinit var findPwValidateCheckFalseTV: TextView
    lateinit var findPwValidateCheckTV: TextView
    lateinit var findPwUserNameET: EditText
    lateinit var findPwEmailET: EditText
    lateinit var findPwCheckTV: TextView
    lateinit var findPwButton: Button
    private var validate = 2 // 아이디 확인값, 확인을 아예 안했을 때 2, 확인은 했지만 맞지 않았을 때 1, 확인이 완료됐을 때 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
            .create(UserService::class.java)
        validateRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build().create(IntroService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)
        findPwValidateButton.setOnClickListener {
            // 텍스트뷰 영역 비활성화
            findPwValidateCheckFalseTV!!.visibility = View.GONE
            findPwUserIdNullTV!!.visibility = View.GONE
            val id = findPwUserIdET!!.text.toString() // 아이디값을 받아옴
            if (id == "") { // 아이디 입력값이 없을 때
                findPwUserIdNullTV!!.visibility = View.VISIBLE
                findPwUserIdNullTV!!.setTextColor(Color.RED)
                findPwUserIdNullTV!!.text = "아이디를 입력해주세요."
                validate = 1 //확인은 했지만 맞지 않았을때
            }
            else {
                validateRetrofit.idValidate(id).enqueue(object : Callback<Boolean> {
                    override fun onResponse(
                        call: Call<Boolean>,
                        response: Response<Boolean>
                    ) {
                        if (response.isSuccessful) { // 성공적으로 받아왔을 때
                            if (response.body() == true) { // 아이디가 있을 때
                                findPwValidateButton!!.setImageResource(R.drawable.ic_register_check)
                                findPwValidateCheckTV.setTextColor(-0xb946a5)
                                validate = 0
                            } else { // 아이디가 없을 때
                                findPwValidateButton!!.setImageResource(R.drawable.ic_register_not_check)
                                findPwUserIdNullTV!!.visibility = View.VISIBLE
                                findPwUserIdNullTV!!.setTextColor(Color.RED)
                                findPwUserIdNullTV!!.text = "존재하지 않는 아이디입니다."
                                validate = 1
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "오류가 발생했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<Boolean>,
                        t: Throwable
                    ) {
                        Toast.makeText(context, "서버 네트워크가 닫혀있습니다.", Toast.LENGTH_LONG)
                            .show()
                        t.printStackTrace()
                    }
                })

            }
        }
        findPwButton.setOnClickListener {
            val userId = findPwUserIdET!!.text.toString()
            var userName = findPwUserNameET.text.toString()
            var email = findPwEmailET.text.toString()

            if (validate == 2) { // 아이디 체크를 완전히 하지 않았을 때
                findPwValidateCheckFalseTV!!.visibility = View.VISIBLE
            }
            // 빈 값이 존재할 경우
            if (userName == "" || email == "") {
                findPwCheckTV!!.visibility = View.VISIBLE // 값을 채워넣으라는 안내문 출력
            } else { // 값을 모두 넣었을 때
                if (validate == 0) {
                    retrofit.findPw(userId, userName, email).enqueue(object : Callback<Boolean> {
                        override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                            if (response.isSuccessful) { // 성공적으로 받아왔을 때
                                if (response.body() == true) {
                                    Toast.makeText(context, "이메일을 확인해주세요", Toast.LENGTH_SHORT)
                                        .show()
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


        }
        return view
    }


    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_pw_find, container, false) as ViewGroup

        findPwUserIdET = view.findViewById(R.id.findPwUserIdET);
        findPwValidateButton = view.findViewById(R.id.findPwValidateButton);
        findPwUserIdNullTV = view.findViewById(R.id.findPwUserIdNullTV);
        findPwValidateCheckFalseTV = view.findViewById(R.id.findPwValidateCheckFalseTV);
        findPwValidateCheckTV = view.findViewById(R.id.findPwValidateCheckTV);
        findPwUserNameET = view.findViewById(R.id.findPwUserNameET);
        findPwEmailET = view.findViewById(R.id.findPwEmailET);
        findPwCheckTV = view.findViewById(R.id.findPwCheckTV);
        findPwButton = view.findViewById(R.id.findPwButton);

        return view
    }
}