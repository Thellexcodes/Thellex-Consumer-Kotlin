package com.thellex.pos.features.fiat

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.thellex.pos.R

class ManageFiatAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dynamic_fiat_account)

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