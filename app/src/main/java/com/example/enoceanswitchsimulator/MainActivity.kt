package com.example.enoceanswitchsimulator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.example.enoceanswitchsimulator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleButton(binding.firstButton, { sendByMotionDown(binding.firstButton.text.toString()) }, { sendByMotionUp(binding.firstButton.text.toString()) })
        handleButton(binding.secondButton, { sendByMotionDown(binding.secondButton.text.toString()) }, { sendByMotionUp(binding.secondButton.text.toString()) })
        handleButton(binding.thirdButton, { sendByMotionDown(binding.thirdButton.text.toString()) }, { sendByMotionUp(binding.thirdButton.text.toString()) })
        handleButton(binding.fourthButton, { sendByMotionDown(binding.fourthButton.text.toString()) }, { sendByMotionUp(binding.fourthButton.text.toString()) })
    }

    private fun sendByMotionDown(buttonName: String) {
        println("$buttonName: poszedl klik w dół")
    }

    private fun sendByMotionUp(buttonName: String) {
        println("$buttonName: poszedł klik w górę")
    }

    private fun handleButton(button: View, actionDown: () -> Unit, actionUp: () -> Unit) {
        button.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.performClick()
                    actionDown()
                }
                MotionEvent.ACTION_UP -> {
                    actionUp()
                }
            }
            return@setOnTouchListener true
        }
    }
}
