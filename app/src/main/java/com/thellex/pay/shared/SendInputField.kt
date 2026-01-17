package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SendInputField(
//    modifier: Modifier = Modifier,
//    value: String,
//    onValueChange: (String) -> Unit,
//    placeholder: String = "",
//    trailingIcon: @Composable (() -> Unit)? = null,
//    readOnly: Boolean = false
//) {
//    Column(modifier = modifier.fillMaxWidth()) {
//        OutlinedTextField(
//            value = value,
//            onValueChange = onValueChange,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(55.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(DarkBlue.copy(alpha = 0.2f)),
//            singleLine = true,
//            readOnly = readOnly,
//            textStyle = TextStyle(
//                color = Color.White,
//                fontSize = 16.sp
//            ),
//            placeholder = {
//                Text(
//                    text = placeholder,
//                    color = SteelBlueGrey,
//                    fontSize = 12.sp,
//                    fontFamily = KumbhSansFontFamily,
//                    fontWeight = FontWeight.Normal
//                )
//            },
//            trailingIcon = trailingIcon,
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                focusedBorderColor = Color.Transparent,
//                unfocusedBorderColor = Color.Transparent,
//                disabledBorderColor = Color.Transparent,
//                errorBorderColor = Color.Transparent,
//                cursorColor = Color.White
//            )
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "0.00",
    enabled: Boolean = true,
    isLoading: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    textStyle: TextStyle = TextStyle(
        color = White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    placeholderStyle: TextStyle = TextStyle(
        color = SteelBlueGrey,
        fontSize = 14.sp,
        fontFamily = KumbhSansFontFamily,
        fontWeight = FontWeight.Normal
    ),
    // Now properly accepts any composable as trailing icon
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val effectiveEnabled = enabled && !isLoading

    OutlinedTextField(
        value = value,
        onValueChange = { newText ->
            // Live sanitization (optional – can be removed or moved to caller)
            val sanitized = newText
                .replace(",", ".")
                .filter { it.isDigit() || it == '.' }

            if (sanitized.count { it == '.' } <= 1) {
                onValueChange(sanitized)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBlue.copy(alpha = 0.25f)),
        enabled = effectiveEnabled,
        readOnly = !effectiveEnabled,
        singleLine = singleLine,
        textStyle = textStyle.copy(
            color = if (effectiveEnabled) White else White.copy(alpha = 0.55f)
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = placeholderStyle
            )
        },
        keyboardOptions = keyboardOptions.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = White,
            unfocusedTextColor = White,
            disabledTextColor = White.copy(alpha = 0.55f)
        ),
        trailingIcon = {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
                trailingIcon != null -> {
                    trailingIcon()
                }
                else -> null
            }
        }
    )
}