package com.thellex.pay.shared

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thellex.pay.R
import com.thellex.pay.core.decorators.White
import com.thellex.pay.data.model.SetPinRequest
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.network.services.ApiClient
import java.io.IOException
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.Transparent

@SuppressLint("RememberReturnType", "ServiceCast", "UseOfNonLambdaOffsetOverload")
@Composable
fun PinEntryContent(
    userViewModel: UserViewModel,
    onPinSuccess: () -> Unit,
    onError: (String) -> Unit = {}
) {
    val TAG = "SecuritySettingsScreen";
    val application = LocalContext.current.applicationContext as Application
    val factory = UserViewModelFactory(application)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val authResult by userViewModel.authResult.observeAsState()
    val authToken by userViewModel.token.observeAsState()
    var shakeTrigger by remember { mutableStateOf(0) }


    val hasPin = authResult?.security?.hasPin ?: false

    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    val backArrowPainter = painterResource(id = R.drawable.icon_arrow_back)

    // Determine current mode
    val isFirstTimeSetup = !hasPin
    val isConfirmationPhase = isFirstTimeSetup && enteredPin.length == maxPinLength
    val currentPin = if (isConfirmationPhase) confirmPin else enteredPin

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val title = when {
        isConfirmationPhase -> "Confirm Transaction Pin"
        isFirstTimeSetup -> "Set Transaction Pin"
        else -> "Enter Transaction Pin"
    }

    val subtitle = when {
        isConfirmationPhase -> "Re-enter your pin to confirm."
        isFirstTimeSetup -> "Set a secure pin for your transactions."
        else -> "Enter your pin to continue."
    }


    // Clear error when typing
    LaunchedEffect(currentPin.length) {
        if (currentPin.isNotEmpty()) errorMessage = null
    }


// Trigger shake + vibration when error is "Incorrect PIN..."
    LaunchedEffect(errorMessage) {
        if (errorMessage?.contains("Incorrect PIN", ignoreCase = true) == true) {
            shakeTrigger++ // Trigger animation
            // Vibration
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        }
    }

    // Shake offset animation
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            // Shake sequence: left → right → left → right → center
            offsetX.animateTo(-12f, animationSpec = tween(50))
            offsetX.animateTo(12f, animationSpec = tween(50))
            offsetX.animateTo(-12f, animationSpec = tween(50))
            offsetX.animateTo(12f, animationSpec = tween(50))
            offsetX.animateTo(0f, animationSpec = tween(50))
        }
    }

    // Handle PIN completion
    LaunchedEffect(currentPin.length) {
        if (currentPin.length == maxPinLength && !isLoading) {
            isLoading = true
            errorMessage = null

            val api = ApiClient.getAuthenticatedApi(application, authToken!!)

            if (isFirstTimeSetup && isConfirmationPhase) {
                if (confirmPin != enteredPin) {
                    errorMessage = "PINs do not match. Please try again."
                    enteredPin = ""
                    confirmPin = ""
                    isLoading = false
                    return@LaunchedEffect
                }

                try {
                    try {
                        val response = api.setSecurityPin(SetPinRequest(enteredPin))
                        if (response.isSuccessful && response.body()?.result == true) {
                            userViewModel.updateSecurityPinDetails(response.body()?.result!!)
                            onPinSuccess()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            errorMessage = when {
                                errorBody?.contains("PIN_NOT_SET") == true -> "Pin not set"
                                else -> "Invalid PIN"
                            }
                            enteredPin = ""
                            confirmPin = ""
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network error. Please try again."
                        enteredPin = ""
                        confirmPin = ""
                    } finally {
                        isLoading = false
                    }
                }catch (e: Exception) {

                }
            } else if (!isFirstTimeSetup) {
                try {
                    val response = api.verifySecurityPin(currentPin)

                    if (response.isSuccessful) {
                        val body = response.body()

                        when {
                            body?.result == true -> {
                                onPinSuccess()
                            }
                            body?.result == false -> {
                                errorMessage = "Incorrect PIN. Please try again."
                                enteredPin = ""
                            }
                            else -> {
                                errorMessage = "Verification failed. Please try again."
                                enteredPin = ""
                            }
                        }
                    } else {
                        // ❌ HTTP error from backend
                        val errorBody = response.errorBody()?.string().orEmpty()

                        errorMessage = when {
                            errorBody.contains("auth/pin-invalid", true) ->
                                "Incorrect PIN. Please try again."

                            errorBody.contains("auth/pin-too-many-attempts", true) ->
                                "Too many attempts. Please try again later."

                            errorBody.contains("auth/pin-expired", true) ->
                                "Your PIN has expired. Please reset it."

                            errorBody.contains("auth/unauthorized", true) ->
                                "Session expired. Please log in again."

                            else ->
                                "Unable to verify PIN. Please try again."
                        }

                        enteredPin = ""
                    }
                } catch (e: IOException) {
                    // 🌐 Network issues
                    errorMessage = "Network error. Please check your connection."
                    enteredPin = ""
                } catch (e: Exception) {
                    // 💥 Unexpected crash-safe fallback
                    Log.e(TAG, "PIN verification failed", e)
                    errorMessage = "Something went wrong. Please try again."
                    enteredPin = ""
                } finally {
                    isLoading = false
                }

            }
        }
    }

    Column(
        modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = title,
            color = White,
            fontSize = 24.sp,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = White,
            fontSize = 12.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }

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
                    .background(DarkBlue)
                    .offset(x = offsetX.value.dp),
            contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(maxPinLength) { index ->
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < currentPin.length) GoldenYellow else Color(0xFF7D8093)
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
                                        when {
                                            label == "back" -> {
                                                if (currentPin.isNotEmpty()) {
                                                    if (isConfirmationPhase) {
                                                        confirmPin = confirmPin.dropLast(1)
                                                    } else {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                }
                                            }

                                            currentPin.length < maxPinLength -> {
                                                val newPin = currentPin + label
                                                if (isConfirmationPhase) {
                                                    confirmPin = newPin
                                                } else {
                                                    enteredPin = newPin
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (label) {
                                    "back" -> {
                                        Image(
                                            painter = backArrowPainter,
                                            contentDescription = "Delete last digit",
                                            modifier = Modifier
                                                .height(12.dp)
                                                .width(12.dp),
                                            colorFilter = ColorFilter.tint(GoldenYellow)
                                        )
                                    }

                                    else -> {
                                        if (label.isNotEmpty()) {
                                            Text(
                                                text = label,
                                                color = White,
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