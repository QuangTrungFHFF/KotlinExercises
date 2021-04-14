package com.example.aboutme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.aboutme.databinding.ActivityMainBinding

private lateinit var binding : ActivityMainBinding
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.visibility = View.INVISIBLE

        binding.button.setOnClickListener {
            if(binding.textView.visibility == View.INVISIBLE){
                binding.textView.visibility = View.VISIBLE
            }else
            {
                binding.textView.visibility = View.INVISIBLE
            }

        }
    }



}