package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.thellex.pay.R
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.Midnight

@Composable
fun AssetChip(
    label: String,
    iconUrl: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = modifier
            .background(
                color = DarkBlue,
                shape = CircleShape
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        IconDisplayer(
            ticker = label,
            iconUrl = iconUrl,
            modifier = Modifier.size(22.dp),
            fallbackRes = R.drawable.icon_usd
        )
    }
}

@Composable
@Preview
fun AssetStackPreview() {
    val assets = listOf(
        "USDC" to "",
        "USDT" to "",
        "BTC" to ""
    )

    Box(
        modifier =  Modifier
            .wrapContentWidth(unbounded = true)
    ) {
        assets.forEachIndexed { index, (label, iconUrl) ->
            AssetChip(
                label = label,
                iconUrl = iconUrl,
                modifier = Modifier
                    .offset(x = (-8 * index).dp)
                    .zIndex((assets.size - index).toFloat())
            )
        }
    }
}
