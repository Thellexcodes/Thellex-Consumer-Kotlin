package com.thellex.pay.core.decorators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppGradientBackground(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

//        Image(
//            painter = painterResource(R.drawable.general_gradient_background),
//            contentDescription = null,
//            modifier = Modifier.matchParentSize(),
//            contentScale = ContentScale.Crop
//        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}
