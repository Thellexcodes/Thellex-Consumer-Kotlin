package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.KumbhSansFontFamily

enum class InfoCardType {
    INFO,
    WARNING,
    ALERT
}

@Composable
fun InfoCard(
    text: String,
    type: InfoCardType = InfoCardType.INFO,
    modifier: Modifier = Modifier
) {
    val (icon, backgroundColor) = when (type) {
        InfoCardType.INFO -> Icons.Default.Info to Color(0xFF1E293B)
        InfoCardType.WARNING -> Icons.Default.Warning to Color(0xFF3A2E00)
        InfoCardType.ALERT -> Icons.Default.Notifications to Color(0xFF3A0F0F)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.height(12.dp).width(12.dp)
            )

            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            text = "This is an info message.",
            type = InfoCardType.INFO
        )

        InfoCard(
            text = "Ensure the address is correct and on the same network.",
            type = InfoCardType.WARNING
        )

        InfoCard(
            text = "Something went wrong. Please try again.",
            type = InfoCardType.ALERT
        )
    }
}
