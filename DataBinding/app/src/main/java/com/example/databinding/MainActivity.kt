package com.example.databinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.databinding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        val myName : MyName = MyName("Bich Ngoc")
        binding.myName = myName

        binding.btSetNickName.setOnClickListener {
            binding.apply {
                myName.nickname = etNickname.text.toString()
                invalidateAll()
            }
        }


    }
}