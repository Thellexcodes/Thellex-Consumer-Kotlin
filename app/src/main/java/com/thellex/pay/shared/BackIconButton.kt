package com.thellex.pay.shared

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.thellex.pay.core.decorators.White

@SuppressLint("ContextCastToActivity")
@Composable
fun BackIconButton(
    modifier: Modifier = Modifier,
    tint: Color = White,
    onBack: (() -> Unit)? = null
) {
    val activity = LocalContext.current as? Activity

    IconButton(
        modifier = modifier,
        onClick = {
            onBack?.invoke() ?: activity?.finish()
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = tint
        )
    }
}
