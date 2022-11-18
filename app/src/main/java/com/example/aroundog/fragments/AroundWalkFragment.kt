package com.example.aroundog.fragments

import RecyclerViewAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.BuildConfig
import com.example.aroundog.Model.GetWalkHistory
import com.example.aroundog.Model.RecyclerViewItem
import com.example.aroundog.R
import com.example.aroundog.Service.NaverMapService
import com.example.aroundog.Service.RetrofitService
import com.example.aroundog.Service.WalkService
import com.example.aroundog.Util
import com.example.aroundog.dto.WalkListDto
import com.google.gson.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AroundWalkFragment : Fragment() {
    private val TAG = "AROUNDWALKFRAGMENT"
    lateinit var mRecyclerView:RecyclerView
    lateinit var mAdapter: RecyclerViewAdapter
    lateinit var mList:ArrayList<RecyclerViewItem?>
    lateinit var userId:String
    var tile:String = ""

    init {
        MainFragment.firstTile.observe(this){
            tile = it
            Log.d(TAG, tile)
            var gsonInstance: Gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java,
                object : JsonDeserializer<LocalDateTime> {
                    override fun deserialize(
                        json: JsonElement?,
                        typeOfT: Type?,
                        context: JsonDeserializationContext?
                    ): LocalDateTime {
                        return LocalDateTime.parse(json!!.asString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                    }
                }).setLenient().create()
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER)
                .addConverterFactory(GsonConverterFactory.create(gsonInstance))
                .build()
                .create(WalkService::class.java)

            retrofit.getWalkListOrderedByGood(userId, tile, totalCount, itemSize).enqueue(object:Callback<List<WalkListDto>>{
                override fun onResponse(
                    call: Call<List<WalkListDto>>,
                    response: Response<List<WalkListDto>>
                ) {
                    if (response.isSuccessful) {

                        var list = response.body()
                        Log.d("sex", list.toString())
                        if (list != null) {
                            list.forEach {
                                var bitmap:Bitmap
                                var byteArray:ByteArray = Base64.decode(it.img, Base64.DEFAULT)
                                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                //에러일경우
                                if(bitmap ==null)
                                    bitmap = BitmapFactory.decodeResource(resources,R.drawable.error2)
                                addItem(userId, it.walkId, bitmap, it.good, it.bad, it.checkGood, it.checkBad, it.second, it.distance, it.address)
                            }

                        }
                        //성공 시 로딩 화면 끄기
                        Util.progressOffInFragment()
                        totalCount += list!!.size
                        mAdapter.notifyItemRangeChanged(totalCount, itemSize)
                    }
                    Log.d("sex", "fail")

                }

                override fun onFailure(call: Call<List<WalkListDto>>, t: Throwable) {
                    Log.d(TAG, "fail", t)
                }
            })
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        //로딩화면
        Util.progressOnInFragment(this)
        //저장된 id 정보 가져오기
        var user_info_pref =
            requireActivity().getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE)
        userId = user_info_pref.getString("id", "error").toString()
    }
    var isLoading:Boolean = false
    lateinit var retrofit:WalkService
    var totalCount = 0
    var itemSize = 5

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:ViewGroup = inflater.inflate(R.layout.fragment_around_walk,container,false) as ViewGroup
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mList = ArrayList<RecyclerViewItem?>()

        mAdapter = RecyclerViewAdapter(mList)
        //구분선 추가
//        mRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        //아이템 사이 패딩 추가
        mRecyclerView.addItemDecoration(RecyclerDecorationHeight(100))


        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)


        mRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            //https://todaycode.tistory.com/12
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItem =
                    (recyclerView.layoutManager as LinearLayoutManager)!!.findLastCompletelyVisibleItemPosition()
                val totalItemCount = recyclerView.adapter!!.itemCount - 1

//                if (lastVisibleItem == totalItemCount) {//마지막 아이템일때
                if (lastVisibleItem == mList.size-1) {//마지막 아이템일때
                    Log.d(TAG, "last item")
                    if (!isLoading) {//로딩중이 아닐때 && 더 로딩할게 있는지도 확인해봐야함
                        Log.d(TAG, "not loading")

                        loadWalkList()
                        isLoading = true
                    }
                }

            }
        })

//        getWalkListFromDB()
        return view
    }
    fun loadWalkList() {
        Log.d(TAG, "in loadWalkList")

        mList.add(null);
        mAdapter.notifyItemInserted(mList.size - 1)
        Handler(Looper.getMainLooper()).postDelayed({

                retrofit.getWalkListOrderedByGood(userId, tile, totalCount + 1, itemSize)
                    .enqueue(object : Callback<List<WalkListDto>> {
                        override fun onResponse(
                            call: Call<List<WalkListDto>>,
                            response: Response<List<WalkListDto>>
                        ) {
                            if (response.isSuccessful) {
//                            mList.removeAt(mList.size - 1)
                                mList.removeLast()
                                mAdapter.notifyItemRemoved(mList.size)

                                var list = response.body()
                                if (list != null) {
                                    list.forEach {
                                        var bitmap: Bitmap
                                        var byteArray: ByteArray =
                                            Base64.decode(it.img, Base64.DEFAULT)
                                        bitmap =
                                            BitmapFactory.decodeByteArray(
                                                byteArray,
                                                0,
                                                byteArray.size
                                            )
                                        //에러일경우
//                            bitmap = BitmapFactory.decodeResource(resources,R.drawable.error2)
                                        addItem(userId, it.walkId, bitmap, it.good, it.bad, it.checkGood, it.checkBad, it.second, it.distance, it.address)
                                    }
                                }
                                totalCount += list!!.size
                                isLoading = false
                                mAdapter.notifyItemRangeChanged(totalCount, itemSize)
                            }
                        }

                        override fun onFailure(call: Call<List<WalkListDto>>, t: Throwable) {
                            Log.d(TAG, "fail", t)
                        }

                    })

        }, 1000)


    }

    /**
     * @param loginUserId : 로그인한 유저 아이디(버튼 클릭 시 필요)
     * @param walkId : 산책 기록 id
     * @param bitmap : 이미지
     * @param good : 좋아요 개수
     * @param bad : 싫어요 개수
     * @param checkGood : 좋아요 눌렀는지 여부
     * @param checkBad : 싫어요 눌렀는지 여부
     * @param second : 산책 시간
     * @param distance : 산책 거리
     */
    fun addItem(loginUserId:String, walkId:Long, bitmap: Bitmap, good:Int, bad:Int, checkGood:Boolean, checkBad:Boolean, second:Long, distance:Long, address:String){
        var item:RecyclerViewItem = RecyclerViewItem(loginUserId, walkId, bitmap, good, bad, checkGood, checkBad, second, distance, address)
        mList.add(item)
    }

    class RecyclerDecorationHeight(height:Int):RecyclerView.ItemDecoration(){
        private var height = height
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            if(parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1){
                outRect.bottom = height
            }
        }
    }
}