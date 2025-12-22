package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBlue.copy(alpha = 0.2f)),
            singleLine = true,
            readOnly = readOnly,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = SteelBlueGrey,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                cursorColor = Color.White
            )
        )
    }
}