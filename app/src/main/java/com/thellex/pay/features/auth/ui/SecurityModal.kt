package com.thellex.pay.features.auth.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White
import com.thellex.pay.features.auth.viewModel.RegisterPasskeyViewModel
import com.thellex.pay.features.auth.viewModel.SecurityViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun SecurityModal(
    securityViewModel: SecurityViewModel,
    registerPasskeyViewModel: RegisterPasskeyViewModel,
    userViewModel: UserViewModel,
    onSetupCompleted: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Enter Your PIN",
    subtitleSetup: String = "Set a PIN for secure access.",
    subtitleConfirm: String = "Confirm your PIN.",
    subtitleError: String = "PINs don’t match. Try again.",
    subtitleLoading: String = "Verifying PIN..."
) {
    val setupSkipped by securityViewModel.setupSkipped.collectAsState()
    val authResult by userViewModel.authResult.observeAsState()
    val hasPin = authResult?.security?.hasPin ?: false

    var pin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var isConfirmStage by rememberSaveable { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Dismiss modal if user skips setup
    LaunchedEffect(setupSkipped) {
        if (setupSkipped) onDismiss()
    }

    // Move to confirmation stage only on first-time PIN setup
    LaunchedEffect(pin) {
        if (!hasPin && pin.length == 4) {
            isConfirmStage = true
        }
    }

    // Handle PIN submission or confirmation
    LaunchedEffect(pin, confirmPin) {
        if (!hasPin && isConfirmStage && confirmPin.length == 4) {
            // First-time setup: confirm PIN matches
            if (pin == confirmPin) {
                isError = false
               val hasPinResponse = securityViewModel.updateSecurityPin(pin = pin, userViewModel.getToken()!!)
                userViewModel.updateHasPin(hasPinResponse)
                userViewModel.authResult
                onSetupCompleted()
            } else {
                isError = true
                delay(800)
                isError = false
                pin = ""
                confirmPin = ""
                isConfirmStage = false
            }
        } else if (hasPin && pin.length == 4) {
            securityViewModel.verifyUserPin(pin, userViewModel.getToken()!!) { success ->
                if (success) {
                    onSetupCompleted()
                } else {
                    isError = true
                    scope.launch {
                        delay(800)
                        isError = false
                        pin = ""
                    }
                }
            }
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Midnight)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* block clicks */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp))
                    .background(Midnight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = White,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            isError -> subtitleError
                            !hasPin && isConfirmStage -> subtitleConfirm
                            else -> subtitleSetup
                        },
                        color = if (isError) Color.Red else SteelBlueGrey,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // PIN dots
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = DarkBlue,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> Color.Red
                                        index < (if (isConfirmStage && !hasPin) confirmPin.length else pin.length) -> GoldenYellow
                                        else -> SteelBlueGrey
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))

            // Number pad
            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("fingerprint", "0", "back")
            )

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val gridWidth = screenWidth * 0.7f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.width(gridWidth)
                ) {
                    buttons.forEach { row ->
                        items(row) { item ->
                            when (item) {
                                "fingerprint" -> {
                                    // Add FIDO2 registration if needed
                                }
                                "back" -> CircleButton(
                                    iconRes = R.drawable.icon_back,
                                    iconSize = 18.dp,
                                    onClick = {
                                        if (isConfirmStage && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                        else if (!isConfirmStage && pin.isNotEmpty()) pin = pin.dropLast(1)
                                    }
                                )
                                else -> CircleButton(
                                    text = item,
                                    onClick = {
                                        val newDigit = item
                                        if (isConfirmStage && !hasPin && confirmPin.length < 4) confirmPin += newDigit
                                        else if (!isConfirmStage && pin.length < 4) pin += newDigit
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CircleButton(
    size: Dp = 59.dp,
    backgroundColor: Color = DarkBlue,
    iconRes: Int? = null,
    iconSize: Dp = 32.dp,
    text: String? = null,
    textSize: TextUnit = 24.sp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
        onClick = onClick,
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified, // preserve SVG colors
                    modifier = Modifier.size(iconSize)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = textSize,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


