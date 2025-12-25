package com.thellex.pay.shared

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thellex.pay.R

@Composable
fun IconDisplayer(
    ticker: String,
    iconUrl: String?,
    modifier: Modifier = Modifier,
    fallbackRes: Int? = null,
) {
    val model: Any? = when {
        ticker.equals("ngn", ignoreCase = true) -> R.drawable.ngn_green
        !iconUrl.isNullOrBlank() -> iconUrl
        fallbackRes != null -> fallbackRes
        else -> null
    }

    AsyncImage(
        model = model,
        contentDescription = "$ticker icon",
        modifier = modifier
            .width(25.dp)
            .height(25.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        placeholder = fallbackRes?.let { painterResource(it) },
        error = fallbackRes?.let { painterResource(it) },
    )
}


