package com.thellex.payments.core.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import com.thellex.payments.databinding.CustomToastLayoutBinding

object CustomToast {

    private var currentToast: Toast? = null

    fun show(
        context: Context,
        title: String = "Notice",
        message: String,
        @ColorInt backgroundColor: Int = Color.WHITE,
        iconResId: Int? = null,
        gravity: Int = Gravity.TOP
    ) {
        // Cancel any active toast
        currentToast?.cancel()

        val inflater = LayoutInflater.from(context)
        val binding = CustomToastLayoutBinding.inflate(inflater)

        // Set content
        binding.toastTitle.text = title
        binding.toastMessage.text = message
        binding.toastContainer.setBackgroundColor(backgroundColor)

        // Handle optional icon
        if (iconResId != null) {
            binding.toastIcon.setImageResource(iconResId)
            binding.toastIcon.visibility = View.VISIBLE
        } else {
            binding.toastIcon.visibility = View.GONE
        }

        // Configure toast
        val toast = Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = binding.root
            setGravity(gravity or Gravity.CENTER_HORIZONTAL, 0, dpToPx(context, 48))
        }

        // Set width to 80% of screen
        val screenWidth = context.resources.displayMetrics.widthPixels
        binding.root.layoutParams = LinearLayout.LayoutParams((screenWidth * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        // Dismiss when user taps close
        binding.toastCloseBtn.setOnClickListener {
            toast.cancel()
        }

        toast.show()
        currentToast = toast
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
