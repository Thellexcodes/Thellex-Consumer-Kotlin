package com.thellex.pay.features.auth.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White

@SuppressLint("RememberReturnType")
@Composable
fun SecuritySettingsScreen(navController: NavHostController? = null) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4
    val backArrowPainter = painterResource(id = R.drawable.icon_arrow_back)

    AppGradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                // Top Section - Fixed ~30% height
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Set Transaction Pin",
                        color = White,
                        fontSize = 24.sp,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Set security for your transactions.",
                        color = White,
                        fontSize = 12.sp,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Bottom Curved Section - Takes remaining ~70%
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 83.dp, topEnd = 83.dp))
                        .background(Midnight)
                        .padding(horizontal = 40.dp, vertical = 40.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // PIN Indicator Dots
                        Box(
                            modifier = Modifier
                                .width(156.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(DarkBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(maxPinLength) { index ->
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index < pin.length) GoldenYellow else Color(0xFF7D8093)
                                            )
                                    )
                                }
                            }
                        }

                        // Numeric Keypad
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val numbers = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("", "0", "back")
                            )


                            numbers.forEachIndexed { index, row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .height(69.dp)
                                                .width(69.dp)
                                                .clip(CircleShape)
                                                .background(if (label.isEmpty()) Transparent else DarkBlue)
                                                .clickable(
                                                    enabled = label.isNotEmpty(),
                                                    indication = rememberRipple(color = GoldenYellow),
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ) {
                                                    if (label == "back") {
                                                        if (pin.isNotEmpty()) {
                                                            pin = pin.dropLast(1)
                                                        }
                                                    } else if (label.isNotEmpty() && pin.length < maxPinLength) {
                                                        pin += label
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // UI rendering goes here — this is composable context
                                            when (label) {
                                                "back" -> {
                                                    Image(
                                                        painter = backArrowPainter,
                                                        contentDescription = "Delete last digit",
                                                        modifier = Modifier.height(13.dp).width(13.dp),
                                                        colorFilter = ColorFilter.tint(GoldenYellow)
                                                    )
                                                }
                                                else -> {
                                                    if (label.isNotEmpty()) {
                                                        Text(
                                                            text = label,
                                                            color = if (label == "back") GoldenYellow else White, // You had "<" before, now using "back"
                                                            fontSize = 24.sp,
                                                            fontWeight = FontWeight.Light,
                                                            fontFamily = OutfitFontFamily
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (index < numbers.size - 1) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(name = "Default", showBackground = false)
@Composable
fun SecuritySettingsScreenPreview() {
    SecuritySettingsScreen()
}