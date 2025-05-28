package com.thellex.pos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thellex.pos.ui.login.LoginPinActivity

class AuthVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_verification)

        val tabEmail: AppCompatButton = findViewById(R.id.tab_email)
        val tabPhone: AppCompatButton = findViewById(R.id.tab_phone)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top,
                view.paddingRight,
                systemBarsInsets.bottom
            )

            insets
        }

        tabEmail.setOnClickListener {
            tabEmail.setBackgroundResource(R.drawable.bg_orange_tab_selected)
            tabPhone.setBackgroundResource(R.drawable.bg_tab_unselected)
            // show email layout, hide phone layout
        }

        tabPhone.setOnClickListener {
            tabPhone.setBackgroundResource(R.drawable.bg_orange_tab_selected)
            tabEmail.setBackgroundResource(R.drawable.bg_tab_unselected)
            // show phone layout, hide email layout
        }


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginPinActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }



    private fun navigateToQuickActions() {
        val intent = Intent(this, LoginPinActivity::class.java)
        startActivity(intent)
        finish()
    }
}