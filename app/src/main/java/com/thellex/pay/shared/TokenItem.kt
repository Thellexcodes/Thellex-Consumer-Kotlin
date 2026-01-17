package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.data.model.TokenInfo

@Composable
fun TokenItem(
    token: TokenInfo,
    selected: Boolean,
    onClick: (TokenInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkBlue)
            .clickable { onClick(token) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconDisplayer(
            ticker = token.symbol.name,
            iconUrl = token.iconDisplay,
            fallbackRes = R.drawable.icon_avatar,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = token.symbol.name.uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = token.name,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        if (selected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FiatTokenItem(
    name: String,
    symbol: String,
    iconUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        IconDisplayer(
            ticker = symbol,
            iconUrl = iconUrl,
            modifier = Modifier.size(40.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = White,
                fontSize = 14.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = symbol,
                color = SteelBlueGrey,
                fontSize = 12.sp,
                fontFamily = KumbhSansFontFamily
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = BrightSkyBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


val SUPPORTED_FIATS = setOf(
    "NGN",
    "USD",
    "EUR",
    "GBP"
)