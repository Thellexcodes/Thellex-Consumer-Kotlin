package com.example.thellex.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.thellex.POSActivity
import com.example.thellex.QuickActions
import com.example.thellex.databinding.ActivityLoginPinBinding

class LoginPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPinBinding
    private val pinBuilder = StringBuilder()
    private lateinit var dotViews: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, POSActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)

//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        dotViews = listOf(
//            findViewById(R.id.dot1),
//            findViewById(R.id.dot2),
//            findViewById(R.id.dot3),
//            findViewById(R.id.dot4)
//        )

//        val keypad = findViewById<GridLayout>(R.id.pin_grid)
//        for (i in 0 until keypad.childCount) {
//            val child = keypad.getChildAt(i)
//            if (child is Button) {
//                child.setOnClickListener {
//                    handleKeyPress(child.text.toString())
//                }
//            }
//        }

    }

    private fun handleKeyPress(input: String) {
        when {
            input == "←" && pinBuilder.isNotEmpty() -> {
                pinBuilder.deleteAt(pinBuilder.length - 1)
            }
            input in "0".."9" && pinBuilder.length < 4 -> {
                pinBuilder.append(input)
            }
        }
        updateDots()

        if (pinBuilder.length == 4) {
            Toast.makeText(this, "PIN Entered: $pinBuilder", Toast.LENGTH_SHORT).show()
            navigateToQuickActions()
        }
    }

    private fun updateDots() {
        for (i in dotViews.indices) {
            if (i < pinBuilder.length) {
                dotViews[i].setTextColor(Color.BLACK) // Dot filled
            } else {
                dotViews[i].setTextColor(Color.GRAY) // Dot empty
            }
        }
    }

    private fun navigateToQuickActions() {
//        val intent = Intent(this, POS::class.java)
//        startActivity(intent)
//        finish()
    }
}