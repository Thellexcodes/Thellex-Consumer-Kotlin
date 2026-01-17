package com.thellex.pay.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.Transparent
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFullWidthModal(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    backgroundColor: Color = Midnight,
    contentColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFF778DA9), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Respect system navigation bar
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title
            title?.let {
                Text(
                    text = it,
                    color = contentColor,
                    fontSize = 18.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Light,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // User-provided content
            content()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(
    name = "AppFullWidthModal - Network Selection",
    showBackground = true,
    backgroundColor = 0xFF0B1020,
    device = Devices.PIXEL_7,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AppFullWidthModalPreview() {
    MaterialTheme {
        // Simulate the modal's appearance in preview (since ModalBottomSheet can't float in preview)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(DarkBlue),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Drag handle (same as in modal)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(Color(0xFF778DA9), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = "Select Network",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Sample content (like your network list)
                Column {
                    Text(
                        text = "BNB Smart Chain (BEP20)",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(vertical = 12.dp)
                    )
                    Divider(color = Color(0xFF415A77))
                    Text(
                        text = "Tron (TRC20)",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(vertical = 12.dp)
                    )
                    Divider(color = Color(0xFF415A77))
                    Text(
                        text = "Ethereum (ERC20)",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}