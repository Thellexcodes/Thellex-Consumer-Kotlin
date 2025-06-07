package com.thellex.pos.features.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.R

class SelectCryptoCurrencyActivity : AppCompatActivity() {
    private lateinit var paymentCard1: View
    private lateinit var paymentCard2: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_payment_method)

        // Initialize views
        paymentCard1 = findViewById(R.id.paymentCard1)
        paymentCard2 = findViewById(R.id.paymentCard2)

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

        val backButton = findViewById<ImageButton>(R.id.select_payment_method_withdraw_back_button)
        backButton.setOnClickListener {
            finish()
        }

        setupClickAnimations()
    }

    private fun setupClickAnimations() {
//        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
//            paymentCard1,
//            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.03f),
//            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.03f)
//        ).setDuration(100)
//
//        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
//            paymentCard1,
//            PropertyValuesHolder.ofFloat("scaleX", 1.03f, 1f),
//            PropertyValuesHolder.ofFloat("scaleY", 1.03f, 1f)
//        ).setDuration(100)

        paymentCard1.setOnClickListener {
//            scaleUp.start()
//            scaleDown.start()
            startActivity(Intent(this, POSWithdrawalSummaryActivity::class.java))
        }

        paymentCard2.setOnClickListener {
//            scaleUp.start()
//            scaleDown.start()
            startActivity(Intent(this, POSWithdrawalSummaryActivity::class.java))
        }
    }
}