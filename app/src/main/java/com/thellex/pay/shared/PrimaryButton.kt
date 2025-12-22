package com.thellex.pay.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily

@Composable
fun PrimaryButton(
    text: String = "CONFIRM",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                color = if (enabled) GoldenYellow else DarkBlue,
                shape = RoundedCornerShape(7.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = KumbhSansFontFamily
        )
    }
}


@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun PrimaryConfirmButtonPreview() {
    PrimaryButton(
        text = "CONFIRM",
        onClick = {},
        modifier = Modifier
            .padding(16.dp)
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun PrimaryConfirmButtonDisabledPreview() {
    PrimaryButton(
        text = "CONFIRM",
        enabled = false,
        onClick = {},
        modifier = Modifier
            .padding(16.dp)
    )
}
