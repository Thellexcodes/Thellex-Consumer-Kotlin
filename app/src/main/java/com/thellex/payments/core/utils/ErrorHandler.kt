package com.thellex.payments.core.utils

import android.content.Context
import com.thellex.payments.R
import com.thellex.payments.core.decorators.color
import com.thellex.payments.data.enums.PaymentErrorEnum
import com.thellex.payments.data.enums.UserErrorEnum

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
