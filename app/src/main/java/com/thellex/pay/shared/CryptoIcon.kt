package com.thellex.pay.shared

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thellex.pay.R

@Composable
fun IconDisplayer(
    ticker: String,
    iconUrl: String? = null,
    modifier: Modifier = Modifier,
    fallbackRes: Int? = null,
    tint: Color? = null
) {
    val model: Any? = when {
        !iconUrl.isNullOrBlank() -> iconUrl
        ticker.equals("ngn", ignoreCase = true) -> R.drawable.ngn_green
        fallbackRes != null -> fallbackRes
        else -> null
    }

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = "$ticker icon",
            modifier = modifier
                .size(25.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            colorFilter = tint?.let { ColorFilter.tint(it) },
            placeholder = fallbackRes?.let { painterResource(it) },
            error = fallbackRes?.let { painterResource(it) },
        )
    }
}



