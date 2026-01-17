package com.thellex.pay.shared

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasonSelectionModalContent(
    selectedReason: String?,
    onReasonSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val reasons = listOf(
        "BILLS",
        "RENT",
        "SCHOOL FEES",
        "MEDICAL",
        "FAMILY SUPPORT",
        "BUSINESS",
        "INVESTMENT",
        "SAVINGS",
        "OTHER"
    )

    // Local state for showing/hiding custom input
    var showCustomInput by remember { mutableStateOf(selectedReason == "OTHER") }

    // Local state for what the user types
    var customReasonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Select Reason",
            style = MaterialTheme.typography.titleMedium,
            color = White,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tell us why you're making this transaction",
            color = SteelBlueGrey,
            fontFamily = KumbhSansFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
        ) {
            items(reasons) { reason ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onReasonSelected(reason)           // update selected reason
                            showCustomInput = (reason == "OTHER")
                            if (reason != "OTHER") {
                                customReasonText = ""      // clear when switching away
                            }
                            // IMPORTANT: NO onDismiss() here anymore!
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reason,
                        color = White,
                        fontWeight = if (reason == selectedReason) FontWeight.Normal else FontWeight.Light,
                        fontSize = 10.sp,
                        fontFamily = KumbhSansFontFamily,
                        modifier = Modifier.weight(1f)
                    )

                    if (reason == selectedReason) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = GoldenYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Custom input appears only for "OTHER" — modal stays open
        AnimatedVisibility(
            visible = showCustomInput,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Please specify the reason",
                    color = SteelBlueGrey,
                    fontFamily = KumbhSansFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = customReasonText,
                    onValueChange = { customReasonText = it },
                    placeholder = { Text("Type here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = White,
                        fontFamily = KumbhSansFontFamily
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SteelBlueGrey.copy(alpha = 0.6f),
                        cursorColor = White,
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f),
                border = BorderStroke(1.dp, SteelBlueGrey),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text("Cancel".uppercase(),
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 10.sp
                )
            }

            Button(
                onClick = {
                    val finalReason = when {
                        selectedReason == "OTHER" && customReasonText.isNotBlank() ->
                            "OTHER: $customReasonText".trim()
                        selectedReason == "OTHER" ->
                            "OTHER"
                        else ->
                            selectedReason ?: "BILLS"
                    }
                    onReasonSelected(finalReason)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenYellow,
                    contentColor = GoldenYellow,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = GoldenYellow
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                enabled = selectedReason != null &&
                        (selectedReason != "OTHER" || customReasonText.isNotBlank())
            ) {
                Text(
                    text = "Confirm".uppercase(),
                    fontFamily = KumbhSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = Midnight
                )
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ReasonSelectionModalContent(
//    selectedReason: String?,
//    onReasonSelected: (String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    val reasons = listOf(
//        "BILLS",
//        "RENT",
//        "SCHOOL FEES",
//        "MEDICAL",
//        "FAMILY SUPPORT",
//        "BUSINESS",
//        "INVESTMENT",
//        "SAVINGS",
//        "OTHER"
//    )
//
//    // Track whether to show the custom input
//    var showCustomInput by remember { mutableStateOf(selectedReason == "OTHER") }
//
//    // The text the user types when OTHER is selected
//    var customReasonText by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 12.dp)
//    ) {
//        // Title
//        Text(
//            text = "Select Reason",
//            style = MaterialTheme.typography.titleMedium,
//            color = White,
//            fontFamily = KumbhSansFontFamily,
//            fontWeight = FontWeight.Bold,
//            fontSize = 16.sp,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        // Subtitle
//        Text(
//            text = "Tell us why you're making this transaction",
//            color = SteelBlueGrey,
//            fontFamily = KumbhSansFontFamily,
//            fontSize = 13.sp,
//            fontWeight = FontWeight.Normal,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        LazyColumn(
//            modifier = Modifier
//                .weight(1f, fill = false)
//                .fillMaxWidth()
//        ) {
//            items(reasons) { reason ->
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            onReasonSelected(reason)
//                            showCustomInput = (reason == "OTHER")
//                            // Clear custom text when switching away from OTHER
//                            if (reason != "OTHER") {
//                                customReasonText = ""
//                            }
//                        }
//                        .padding(vertical = 14.dp, horizontal = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = reason,
//                        color = if (reason == selectedReason) MaterialTheme.colorScheme.primary else White,
//                        fontWeight = if (reason == selectedReason) FontWeight.Bold else FontWeight.Normal,
//                        fontSize = 14.sp,
//                        fontFamily = KumbhSansFontFamily,
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    if (reason == selectedReason) {
//                        Icon(
//                            imageVector = Icons.Default.Check,
//                            contentDescription = "Selected",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//
//                Divider(
//                    color = DarkBlue,
//                    thickness = 1.dp,
//                    modifier = Modifier.padding(start = 8.dp)
//                )
//            }
//        }
//
//        // Custom input – appears only when "OTHER" is selected
//        AnimatedVisibility(
//            visible = showCustomInput,
//            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
//            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
//        ) {
//            Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
//                Text(
//                    text = "Please specify the reason",
//                    color = SteelBlueGrey,
//                    fontFamily = KumbhSansFontFamily,
//                    fontSize = 13.sp,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//
//                OutlinedTextField(
//                    value = customReasonText,
//                    onValueChange = { customReasonText = it },
//                    placeholder = { Text("Type here...") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp),
//                    maxLines = 4,
//                    textStyle = TextStyle(
//                        fontSize = 15.sp,
//                        color = White,
//                        fontFamily = KumbhSansFontFamily
//                    ),
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = MaterialTheme.colorScheme.primary,
//                        unfocusedBorderColor = SteelBlueGrey.copy(alpha = 0.6f),
//                        cursorColor = White,
//                    )
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Confirm & Cancel buttons
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            OutlinedButton(
//                onClick = onDismiss,
//                modifier = Modifier
//                    .weight(1f)
//                    .height(48.dp),
//                border = BorderStroke(1.dp, SteelBlueGrey),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = White)
//            ) {
//                Text("Cancel", fontFamily = KumbhSansFontFamily)
//            }
//
//            Button(
//                onClick = {
//                    val finalReason = when {
//                        selectedReason == "OTHER" && customReasonText.isNotBlank() ->
//                            "OTHER: $customReasonText".trim()
//                        selectedReason == "OTHER" ->
//                            "OTHER"  // or you can prevent confirm if empty
//                        else ->
//                            selectedReason ?: "BILLS"
//                    }
//                    onReasonSelected(finalReason)
//                    onDismiss()  // only close here — after confirm
//                },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(48.dp),
//                enabled = selectedReason != null &&
//                        (selectedReason != "OTHER" || customReasonText.isNotBlank())
//            ) {
//                Text("Confirm", fontFamily = KumbhSansFontFamily)
//            }
//        }
//    }
//}

@Preview(name = "No selection", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ReasonModal_NoSelection_Preview() {
        Surface(modifier = Modifier.fillMaxWidth(), color = Midnight) {
            ReasonSelectionModalContent(
                selectedReason = null,
                onReasonSelected = {},
                onDismiss = {}
            )
    }
}

@Preview(name = "Regular reason selected", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ReasonModal_RegularSelected_Preview() {
        Surface(modifier = Modifier.fillMaxWidth(), color = Midnight) {
            ReasonSelectionModalContent(
                selectedReason = "SCHOOL FEES",
                onReasonSelected = {},
                onDismiss = {}
            )
        }
}

@Preview(name = "OTHER selected + custom text", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ReasonModal_OtherWithText_Preview() {
        Surface(modifier = Modifier.fillMaxWidth(), color = Midnight) {
            ReasonSelectionModalContent(
                selectedReason = "OTHER",
                onReasonSelected = {},
                onDismiss = {}
            )
        }
}

@Preview(name = "OTHER selected + empty text", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ReasonModal_OtherEmpty_Preview() {
        Surface(modifier = Modifier.fillMaxWidth(), color = Midnight) {
            ReasonSelectionModalContent(
                selectedReason = "OTHER",
                onReasonSelected = {},
                onDismiss = {}
            )
        }
}