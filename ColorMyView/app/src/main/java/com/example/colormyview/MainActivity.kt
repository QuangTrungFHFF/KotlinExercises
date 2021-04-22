package com.example.colormyview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.colormyview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()


    }

    private fun setListener() {
        val ClickableViews : List<View> =
            listOf(binding.boxOneText, binding.boxTwoText, binding.boxThreeText, binding.boxFourText, binding.boxFiveText)

        for(item in ClickableViews){
            item.setOnClickListener { makecolor(it) }
        }
    }

    private fun makecolor(view: View) {
        when(view){
            // Boxes using Color class colors for background
            binding.boxOneText -> view.setBackgroundColor(Color.DKGRAY)
            binding.boxTwoText -> view.setBackgroundColor(Color.GRAY)

            // Boxes using Android color resources for background
            binding.boxThreeText -> view.setBackgroundResource(android.R.color.holo_green_light)
            binding.boxFourText -> view.setBackgroundResource(android.R.color.holo_green_dark)
            binding.boxFiveText -> view.setBackgroundResource(android.R.color.holo_green_light)

            else -> view.setBackgroundColor(Color.LTGRAY)
        }
    }
}