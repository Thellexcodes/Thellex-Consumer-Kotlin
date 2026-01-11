package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.White
import com.thellex.pay.features.auth.viewModel.UserViewModel

@Composable
fun NotificationIconWithBadge(
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopEnd
    ) {

        // Main notification icon
        IconDisplayer(
            ticker = "Notification",
            iconUrl = "",
            fallbackRes = R.drawable.icon_notification,
            modifier = Modifier.size(24.dp)
        )

        // Badge (only show if count > 0)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(14.dp)
                    .background(
                        color = BrightSkyBlue,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NotificationIconWithBadgePreview() {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        NotificationIconWithBadge(
            count = 3
        )
    }
}
