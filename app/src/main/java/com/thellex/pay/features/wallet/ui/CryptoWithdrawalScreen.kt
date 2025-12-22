package com.thellex.pay.features.wallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.BrightSkyBlue
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.PrimaryButton

@SuppressLint("ContextCastToActivity")
@Composable
fun CryptoWithdrawalScreen(
    navController: NavHostController,
) {
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEND TO",
                        color = Color.White,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Image(
                        painter = painterResource(id = R.drawable.icon_qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier.height(32.dp).width(32.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    SendInputField(
                        modifier = Modifier,
                        value = "",
                        onValueChange = { },
                        placeholder = "Enter Wallet Address",
                        trailingIcon = { }
                    )
                    Spacer(modifier = Modifier.height(19.dp))
                    DarkDropdownSelect(
                        placeholder = "Select Network",
                        selected = null,
                        onSelect = {}
                    )
                    Spacer(modifier = Modifier.height(19.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepNavy)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "AMOUNT",
                                color = White,
                                fontSize = 10.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Light
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SendInputField(
                                    modifier = Modifier.weight(1f),
                                    value = "",
                                    onValueChange = { },
                                    placeholder = "Enter Amount",
                                    trailingIcon = {}
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBlue)
                                        .padding(horizontal = 5.dp),
                                        contentAlignment = Alignment.Center
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        IconDisplayer(
                                            ticker = "",
                                            iconUrl = "https://assets.coingecko.com/coins/images/6319/standard/usdc.png",
                                            fallbackRes = R.drawable.icon_usd
                                        )
                                        Text(
                                            text = "USD",
                                            color = White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = KumbhSansFontFamily
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select token",
                                            tint = Color.White,
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row{
                                    Text(
                                        text = "Available Balance : ",
                                        color = White,
                                        fontFamily = KumbhSansFontFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "111111",
                                        color = White,
                                        fontSize = 10.sp,
                                        fontFamily = KumbhSansFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                MaxButton(
                                    modifier = Modifier.width(41.dp).height(22.dp),
                                    onClick = { }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        modifier = Modifier,
                        text = "CONFIRM",
                        onClick = {},
                        enabled = true
                    )
                }
            }
        }
    }
}

@Composable
fun CryptoWithdrawalScreenRoute(
    navController: NavHostController,
) {
    CryptoWithdrawalScreen(navController)
}


@Preview(showBackground = true)
@Composable
fun CryptoWithdrawalScreenPreview() {
    CryptoWithdrawalScreen(
        navController = rememberNavController(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendInputField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBlue.copy(alpha = 0.2f)),
            singleLine = true,
            readOnly = readOnly,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = SteelBlueGrey,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                cursorColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkDropdownSelect(
    placeholder: String,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showLeadingIcon: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBlue.copy(alpha = 0.2f))
            .clickable(enabled = enabled) { }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (showLeadingIcon && leadingIcon != null) {
                    leadingIcon()
                }

                Text(
                    text = selected ?: placeholder,
                    color = if (selected == null) SteelBlueGrey else Color.White,
                    fontSize = 12.sp,
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
fun MaxButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BrightSkyBlue)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MAX",
            color = Color.White,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
