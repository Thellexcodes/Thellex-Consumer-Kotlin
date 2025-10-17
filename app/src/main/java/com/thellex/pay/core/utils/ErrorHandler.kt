package com.thellex.pay.core.utils

import android.content.Context
import com.thellex.pay.R
import com.thellex.pay.core.decorators.color
import com.thellex.pay.data.enums.PaymentErrorEnum
import com.thellex.pay.data.enums.UserErrorEnum

interface AppError {
    val code: String
    val message: String
}

object ErrorHandler {
    fun handle(context: Context, title: String, error: AppError?) {
        val message = when (error) {
            is UserErrorEnum -> error.message
            is PaymentErrorEnum -> error.message
            else -> error?.message ?: "An unexpected error occurred."
        }
        CustomToast.show(
            context,
            title = title,
            message = message,
            backgroundColor = context.color(R.color.white),
            iconResId = R.drawable.icon_info_circle
        )
    }
}
