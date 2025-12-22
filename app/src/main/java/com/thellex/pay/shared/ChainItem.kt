package com.thellex.pay.shared


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.data.model.ChainInfo

@Composable
fun ChainItem(
    chain: ChainInfo,
    onClick: (ChainInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DarkBlue,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick(chain) }
            .padding(16.dp)
    ) {
        Text(
            text = chain.displayName,
            color = Color.White,
            fontFamily = KumbhSansFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Fee ${chain.fee}",
            color = SteelBlueGrey,
            fontFamily = KumbhSansFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )

        Text(
            text = "Minimum Withdrawal ${chain.minimumWithdrawal}",
            color = SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal
        )

        Text(
            text = "Arrival Time ${chain.arrivalTime}",
            color = SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}