package com.example.diceroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.diceroller.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rollButton.text = "Let's roll"
        binding.rollButton.setOnClickListener{

            rollDice()
            Toast.makeText(this,"Dice Rolled!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun rollDice() {
        val randomNumber: Int = Random.nextInt(6) + 1
        val drawableDiceResource = when (randomNumber){
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }
        binding.tvDice.setImageResource(drawableDiceResource)
    }
}