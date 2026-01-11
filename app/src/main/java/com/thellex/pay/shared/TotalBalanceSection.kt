package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.White

@Composable
fun TotalBalanceSection(
    balance: String = "$290.79"
) {
    var isBalanceVisible by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Total Balance",
                color = White,
                fontSize = 10.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconDisplayer(
                ticker = "eye",
                iconUrl = "",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { isBalanceVisible = !isBalanceVisible },
                fallbackRes = if (isBalanceVisible)
                    R.drawable.icon_eye_open
                else
                    R.drawable.icon_eye_closed_svg
            )
        }

        Text(
            text = if (isBalanceVisible) balance else "••••••",
            color = GoldenYellow,
            fontSize = 24.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(
    name = "Total Balance – Visible",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun TotalBalanceSectionPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        TotalBalanceSection(balance = "$290.79")
    }
}
