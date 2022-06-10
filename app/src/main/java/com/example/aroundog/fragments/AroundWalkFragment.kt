package com.example.aroundog.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroundog.BuildConfig
import com.example.aroundog.Model.GetWalkHistory
import com.example.aroundog.Model.RecyclerViewAdapter
import com.example.aroundog.Model.RecyclerViewItem
import com.example.aroundog.R
import com.example.aroundog.Service.RetrofitService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AroundWalkFragment : Fragment() {
    lateinit var mRecyclerView:RecyclerView
    lateinit var mAdapter:RecyclerViewAdapter
    lateinit var mList:ArrayList<RecyclerViewItem>
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:ViewGroup = inflater.inflate(R.layout.fragment_around_walk,container,false) as ViewGroup
        mRecyclerView = view.findViewById(R.id.recyclerView)
        mList = ArrayList<RecyclerViewItem>()

        mAdapter = RecyclerViewAdapter(mList)
        //구분선 추가
        mRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        //아이템 사이 패딩 추가
        mRecyclerView.addItemDecoration(RecyclerDecorationHeight(100))


        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        getWalkListFromDB()


        return view
    }
    fun getWalkListFromDB(){
        //https://camposha.info/android-examples/android-mysql-fast-networking-library-images-text/#gsc.tab=0
        var gsonInstance: Gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
        val retrofitAPI = retrofit.create(RetrofitService::class.java)

        retrofitAPI.getWalkHistory().enqueue(object : Callback<ArrayList<GetWalkHistory>> {
            override fun onResponse(call: Call<ArrayList<GetWalkHistory>>, response: Response<ArrayList<GetWalkHistory>>) {
                if(response.isSuccessful){
                    Log.d("sex", "hihi" + response.body().toString())
                    walkHistoryList(response.body()!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<GetWalkHistory>>, t: Throwable) {

            }
        })
    }

    fun walkHistoryList(walkHistoryList: ArrayList<GetWalkHistory>){
        var iterator = walkHistoryList.iterator()
        while (iterator.hasNext()){
            var walkHistory:GetWalkHistory = iterator.next()
            var bitmap:Bitmap

            if(walkHistory.imgSrc != "error"){
                var byteArray:ByteArray = Base64.decode(walkHistory.imgFile, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            } else {
                bitmap = BitmapFactory.decodeResource(resources,R.drawable.error2)
            }
            addItem(walkHistory.serialNumber,bitmap, walkHistory.userId, walkHistory.good.toInt(), walkHistory.bad.toInt())
        }
        mAdapter.notifyDataSetChanged()
    }

    fun addItem(serialNumber:String, bitmap: Bitmap, userId:String, good:Int, bad:Int){
        var item:RecyclerViewItem = RecyclerViewItem(serialNumber, bitmap, userId, good, bad)
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