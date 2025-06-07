package com.thellex.pos.features.pos.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.R

class POSWithdrawalSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal_summary)

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
}