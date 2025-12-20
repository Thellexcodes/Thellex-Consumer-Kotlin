package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.White
import com.thellex.pay.features.admin.TransactionItem
import com.thellex.pay.shared.IconDisplayer

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = modifier
                    .width(45.dp)
                    .height(45.dp)
                    .clip(CircleShape)
                    .background(DarkBlue)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@SuppressLint("ContextCastToActivity")
@Composable
fun AssetDetailScreen(
    navController: NavHostController? = null,
    assetCode: String
){
    val onBackClick = { navController?.popBackStack() }
    val onDepositClick = { //navController.navigate("deposit_screen")
         }
    val onSendClick = { //navController.navigate("send_screen")
         }
    val onConvertClick = { //navController.navigate("convert_screen")
         }
    val onSeeAllClick = { //navController.navigate("transaction_history")
         }


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
                val activity = LocalContext.current as? Activity

                IconButton(onClick = {activity?.finish()}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(19.dp))

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            color = White,
                            fontSize = 10.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconDisplayer(
                            ticker = "",
                            iconUrl = "",
                            fallbackRes = R.drawable.icon_eye_open,
                            modifier = Modifier.width(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = " USD".uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        icon = Icons.Default.Add,
                        label = "Deposit",
                        onClick = onDepositClick
                    )
                    ActionButton(
                        icon = Icons.Default.Send,
                        label = "Send",
                        onClick = onSendClick
                    )
                    ActionButton(
                        icon = Icons.Default.Refresh,
                        label = "Convert",
                        onClick = onConvertClick
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Your Assets",
                        color = Color.White,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "SEE ALL",
                        color = Color.White,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        TransactionItem(
                            title = "Deducted from Card Balance",
                            amount = "₦20,000",
                            time = "Today, 11:28 PM",
                            assetCode
                        )
                    }
                    item {
                        TransactionItem(
                            title = "Deducted from Card Balance",
                            amount = "₦20,000",
                            time = "Today, 11:28 PM",
                            assetCode
                        )
                    }
                    item {
                        TransactionItem(
                            title = "Deducted from Card Balance",
                            amount = "₦20,000",
                            time = "Today, 11:28 PM",
                            assetCode
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun AssetDetailScreenRoute(
    navController: NavHostController,
    assetCode: String
) {
    AssetDetailScreen(navController, assetCode = assetCode)
}


@Preview(showBackground = true)
@Composable
fun AssetDetailScreenPreview() {
    AssetDetailScreen(assetCode = "USD")
}

// Reusable Transaction Item
@Composable
fun TransactionItem(
    title: String,
    amount: String,
    time: String,
    assetCode: String,
    iconRes: Int = R.drawable.icon_avatar
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDisplayer(
                ticker = assetCode,
                iconUrl = "",
                fallbackRes = R.drawable.icon_avatar
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = time,
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Text(
            text = amount,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = OutfitFontFamily,
        )
    }
}