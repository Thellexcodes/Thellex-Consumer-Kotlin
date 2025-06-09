package com.thellex.pos.features.auth.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.databinding.ActivityLoginPinBinding

class LoginPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPinBinding
    private val pinBuilder = StringBuilder()
    private lateinit var dotViews: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rootView = findViewById<View>(android.R.id.content)
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }
        val navBarHeight = resources.getIdentifier("navigation_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }

        // Apply bottom padding equal to navigation bar height
        rootView.setPadding(
            rootView.paddingLeft,
            statusBarHeight,
            rootView.paddingRight,
            navBarHeight
        )
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