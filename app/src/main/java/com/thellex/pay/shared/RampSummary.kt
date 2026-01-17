package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.format
import com.thellex.pay.data.model.ChainInfoDto


@Composable
fun SummaryContent(
    isOnRamp: Boolean,
    chain: ChainInfoDto?,
    fromToken: Any?,
    toToken: Any?,
    amount: String
) {
    // Keep your existing Accordion usage, but update SummaryContent like this:

    // Inside On_Off_RampScreen
    val serviceFeeNgn = 2057.21f   // calculate from input + fee
    val serviceFeeUsd = 15.23f
    val minAmount = 2057.21f
    val maxAmount = 212057.21f
    val spentAmount = 2000f
    val totalLimit = 500000f
//    val spentProgress = spentAmount / totalLimit  // 0f..1f

// Or make them derived from real state:
    val spentProgress by remember(spentAmount, totalLimit) {
        derivedStateOf { (spentAmount / totalLimit).coerceIn(0f, 1f) }
    }

    Accordion(
        title = "SUMMARY",
        initiallyExpanded = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlue)
                .padding(16.dp)
        ) {
            // Service fee row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service fee",
                    color = SteelBlueGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₦${serviceFeeNgn.format(2)}",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${serviceFeeUsd.format(2)}",
                        color = SteelBlueGrey,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rate with timer  info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rate info",
                        tint = GoldenYellow,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "Rate",
                        color = White,
                        fontSize = 12.sp
                    )

                    Box(
                        modifier = Modifier
                            .background(DarkBlue, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "00:59",
                            color = White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "= 3,477.94 NGN/USDT",
                        color = White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = KumbhSansFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spent progress bar section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPENT",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₦${spentAmount.format(2)} / ₦${totalLimit.format(0)}",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { spentProgress }, // 0f..1f
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = GoldenYellow,              // orange progress
                    trackColor = DarkBlue.copy(alpha = 0.6f)
                )
            }
        }
    }
//    Column(modifier = Modifier.padding(16.dp)) {
//        SummaryLine("Service fee", "~${chain?.fee ?: 0f} ${if (isOnRamp) "USD" else "token"}")
//        SummaryLine("Arrival time", chain?.arrivalTime ?: "—")
//        SummaryLine("Rate", "≈ 1 USDT = 3,478 NGN")
//    }
}
