package com.thellex.payments.core.utils

import android.widget.EditText
import com.thellex.payments.R

object Validator {
    fun validateNonEmpty(editText: EditText, text: String): Boolean {
        return if (text.isBlank()) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
            false
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
            true
        }
    }

    fun validateDigitsOnly(editText: EditText, text: String): Boolean {
        return if (!text.all { it.isDigit() }) {
            editText.setBackgroundResource(R.drawable.bg_edittext_error)
            false
        } else {
            editText.setBackgroundResource(R.drawable.rounded_edittext)
            true
        }
    }
}
