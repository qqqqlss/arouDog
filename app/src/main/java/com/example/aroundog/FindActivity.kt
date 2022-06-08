package com.example.aroundog

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.aroundog.fragments.IdFindFragment
import com.example.aroundog.fragments.PwFindFragment

class FindActivity : AppCompatActivity() {
    private val FRAGMENT_ID_FIND = 1
    private val FRAGMENT_PW_FIND = 2
    private var find_id: Button = findViewById(R.id.find_id)
    private var find_pw: Button = findViewById(R.id.find_pw)
    private var find_id_line: View = findViewById(R.id.find_id_line)
    private var find_pw_line: View = findViewById(R.id.find_pw_line)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)
        val toolbar = findViewById<Toolbar>(R.id.find_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
        find_id.setOnClickListener(View.OnClickListener {
            find_id.setTextColor(-0xb946a5)
            find_id.setTypeface(find_pw.getTypeface(), Typeface.BOLD)
            find_id_line.setBackgroundColor(-0xb946a5)
            find_pw.setTextColor(-0x4ca5958e)
            find_pw.setTypeface(find_pw.getTypeface(), Typeface.NORMAL)
            find_pw_line.setBackgroundColor(-0x4ca5958e)
            FragmentView(FRAGMENT_ID_FIND)
        })
        find_pw.setOnClickListener(View.OnClickListener {
            find_id.setTextColor(-0x4ca5958e)
            find_id.setTypeface(find_pw.getTypeface(), Typeface.NORMAL)
            find_id_line.setBackgroundColor(-0x4ca5958e)
            find_pw.setTextColor(-0xb946a5)
            find_pw.setTypeface(find_pw.getTypeface(), Typeface.BOLD)
            find_pw_line.setBackgroundColor(-0xb946a5)
            FragmentView(FRAGMENT_PW_FIND)
        })
        FragmentView(FRAGMENT_ID_FIND)
    }

    private fun FragmentView(fragment: Int) {

        // FragmentTransactiom를 이용해 프래그먼트를 사용
        val transaction = supportFragmentManager.beginTransaction()
        when (fragment) {
            1 -> {
                // 아이디 찾기 프래그먼트 호출
                val idFindFragment = IdFindFragment()
                transaction.replace(R.id.find_container, idFindFragment)
                transaction.commit()
            }
            2 -> {
                // 비밀번호 찾기 프래그먼트 호출
                val pwFindFragment = PwFindFragment()
                transaction.replace(R.id.find_container, pwFindFragment)
                transaction.commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}