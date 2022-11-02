package com.example.aroundog.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.example.aroundog.BuildConfig
import com.example.aroundog.DogEditActivity
import com.example.aroundog.Model.DogSliderAdapter
import com.example.aroundog.R
import com.example.aroundog.Service.DogService
import com.example.aroundog.Service.WalkService
import com.example.aroundog.dto.DogDto
import com.example.aroundog.dto.ImgDto
import com.example.aroundog.dto.ImgDtoUri
import com.example.aroundog.dto.UpdateDogImageDto
import com.example.aroundog.utils.DogBreedData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*


class DogFragment : Fragment() {
    private var dogDto:DogDto? = null


    lateinit var imgViewPager:ViewPager2
    lateinit var profileDogNameTV:TextView
    lateinit var profileDogGenderTV:TextView
    lateinit var profileDogAgeTV:TextView
    lateinit var profileDogHeightTV:TextView
    lateinit var profileDogWeightTV:TextView
    lateinit var profileDogBreedTV:TextView
    lateinit var profileDogInfoLayout:LinearLayout
    lateinit var profileDogConfig:ImageButton
    var hasDog = false
    private val DEFAULT_GALLERY_REQUEST_CODE =200
    lateinit var listener: DogSliderAdapter.ItemClickListener
    lateinit var clickView:View
    lateinit var dogImgList:MutableList<ImgDto>
    lateinit var adapter: DogSliderAdapter
    var clickPosition: Int? = null
    var dogImgListUri:MutableList<ImgDtoUri> = mutableListOf()

    lateinit var retrofit:Retrofit
    lateinit var dogService: DogService
    var dogId:Long = -100L

    init {
        DogEditActivity.editDogInfo.observe(this){
            updateDogInfo(it)
            dogDto = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hasDog = it.getBoolean("hasDog")
            if(hasDog){//등록된 강아지가 있는 경우
                dogDto = it.getSerializable("dog") as DogDto
                dogId = dogDto!!.dogId
                dogImgList = dogDto!!.dogImgList

                //이미지를 캐시디렉터리에 저장 후 Uri 리스트에 추가
                for (imgDto in dogImgList) {
                    try {
                        //dogImgUri저장
                        var dogUri = saveBitmap(imgDto)
                        //이미지 uri 리스트에 추가
                        dogImgListUri.add(dogUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                dogImgList.clear()//얘가 값이 커서 오류났기때문에 클리어해줌
            }
            else{//강아지가 없는 경우 초기화
//                dogImgList = mutableListOf()
            }
        }
        
        //강아지 추가 이미지에 사용할 원클릭 리스너
        listener = object:DogSliderAdapter.ItemClickListener{
            override fun onItemClicked(view: View, position:Int) {
                clickView = view
                clickPosition = position
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.setType("image/")
                startActivityForResult(intent, DEFAULT_GALLERY_REQUEST_CODE)
            }
        }

        var gsonInstance: Gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()

        dogService = retrofit.create(DogService::class.java)

    }

    private fun saveBitmap(imgDto: ImgDto): ImgDtoUri {
        var byteArray: ByteArray =
            android.util.Base64.decode(imgDto.img, android.util.Base64.DEFAULT)
        var bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        val path = File(context!!.cacheDir, "images") //캐시폴더의 images/
        path.mkdirs()//없으면 생성
        val tempFile = File(path, imgDto.path)
        tempFile.createNewFile()//파일 저장
        val out = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)//파일에 쓰기
        out.close()

        val contentUri = FileProvider.getUriForFile(
            context!!,
            "com.example.android.provider",
            tempFile
        )
        var dogUri = ImgDtoUri(imgDto.id, imgDto.path, contentUri)
        return dogUri
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: ViewGroup = setView(inflater, container)

        if(hasDog){//강아지가 있는 경우
            updateDogInfo(dogDto!!)

            //dogDto.dogId != null && dogDto.dogImgList == null : 개는 있는데 사진이 없음
            if (dogImgListUri.isNullOrEmpty()) {
                dogImgListUri.add(ImgDtoUri(-100, "emptyImg", Uri.parse("emptyImg")))

            }else{//강아지와 사진 다 있음
                dogImgListUri.add(ImgDtoUri(-100, "emptyImg", Uri.parse("emptyImg")))//마지막에 사진 추가 이미지
            }

        }else{//강아지가 없는 경우
            dogImgListUri.add(ImgDtoUri(-200,"emptyDog", Uri.parse("emptyDog")))
            profileDogInfoLayout.visibility = View.INVISIBLE
        }
        adapter = DogSliderAdapter(dogImgListUri)
        adapter.adapterListener = listener
        imgViewPager.adapter = adapter

        profileDogConfig.setOnClickListener{
            var intent = Intent(context, DogEditActivity::class.java)
            intent.putExtra("info", dogDto)
            it.context.startActivity(intent)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstanceWithDog(dog:DogDto) =
            DogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("dog", dog)
                    putBoolean("hasDog", true)
                }
            }

        @JvmStatic
        fun newInstanceAddDog() =
            DogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("hasDog", false)
                }
            }
    }

    private fun updateDogInfo(dogDto: DogDto) {
        profileDogNameTV.text = dogDto?.dogName
        profileDogGenderTV.text = dogDto?.dogGender.toString()
        profileDogAgeTV.text = dogDto?.dogAge.toString()
        profileDogHeightTV.text = dogDto?.dogHeight.toString()
        profileDogWeightTV.text = dogDto?.dogWeight.toString()
        var dogBreed = dogDto?.breed
        profileDogBreedTV.text = DogBreedData.getBreed(dogBreed)
    }

    private fun setView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ViewGroup {
        val view: ViewGroup =
            inflater.inflate(R.layout.fragment_dog, container, false) as ViewGroup

        imgViewPager = view.findViewById(R.id.imgViewPager)
        profileDogNameTV = view.findViewById(R.id.profileDogNameTV)
        profileDogGenderTV = view.findViewById(R.id.profileDogGenderTV)
        profileDogAgeTV = view.findViewById(R.id.profileDogAgeTV)
        profileDogHeightTV = view.findViewById(R.id.profileDogHeightTV)
        profileDogWeightTV = view.findViewById(R.id.profileDogWeightTV)
        profileDogBreedTV = view.findViewById(R.id.profileDogBreedTV)
        profileDogInfoLayout = view.findViewById(R.id.profileDogInfoLayout)
        profileDogConfig = view.findViewById(R.id.profileDogConfig)

        return view
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            DEFAULT_GALLERY_REQUEST_CODE -> {
                data ?: return

                //갤러리에서 고른 사진의 uri
                var photo = data.data as Uri

                //확장자
                var cr = context!!.contentResolver
                var type = MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(photo))

                var imgType = listOf<String>("png", "jpg", "jpeg")

                //png, jpg, jpeg일때만 실행
                if (imgType.contains(type)) {
                    try {
                        //byte[] 로 변경
                        var photoArr = context!!.contentResolver.openInputStream(photo)?.buffered()
                            ?.use { it.readBytes() }

                        //multipart 설정
                        var requestFile =
                            RequestBody.create(MediaType.parse("multipart/form-data"), photoArr)
                        var multipartImg: MultipartBody.Part =
                            MultipartBody.Part.createFormData(
                                "image",
                                photo.toString() + ".${type}",
                                requestFile
                            )

                        //retrofit
                        dogService.updateDogImg(dogId, multipartImg)
                            .enqueue(object : Callback<UpdateDogImageDto> {
                                override fun onResponse(
                                    call: Call<UpdateDogImageDto>,
                                    response: Response<UpdateDogImageDto>
                                ) {
                                    if (response.isSuccessful) {
                                        var updateDogImageDto = response.body()
                                        if (updateDogImageDto!!.dogImgId != -100L) {//-100L이면 서버에서 오류난거
                                            //클릭한 이미지 삭제(강아지 사진 추가 이미지)
                                            adapter.notifyItemRemoved(clickPosition!!)
                                            adapter.notifyItemRangeChanged(clickPosition!!, dogImgListUri.size)
                                            dogImgListUri.removeAt(clickPosition!!)

                                            //이미지 추가
                                            var imgDto = ImgDtoUri(
                                                updateDogImageDto!!.dogImgId,
                                                updateDogImageDto.path,
                                                photo
                                            )
                                            dogImgListUri.add(clickPosition!!, imgDto)

                                            //마지막 위치에 강아지 사진 추가 이미지 추가
                                            if (dogImgListUri.last().id != -100L) {
                                                dogImgListUri.add(
                                                    ImgDtoUri(
                                                        -100,
                                                        "emptyImg",
                                                        Uri.parse("emptyImg")
                                                    )
                                                )
                                                adapter.notifyItemInserted(dogImgListUri.lastIndex)
                                            }
                                            adapter.notifyItemInserted(clickPosition!!)
                                        }else {
                                            Toast.makeText(context, "사진추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else {
                                        Toast.makeText(context, "사진추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(
                                    call: Call<UpdateDogImageDto>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(context, "사진추가에 실패했습니다.", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            })

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }else{
                    Toast.makeText(context, "지원하지 않는 파일 형식입니다.", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }
}