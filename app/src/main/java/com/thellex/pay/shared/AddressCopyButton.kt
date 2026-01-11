package com.thellex.pay.shared

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.KumbhSansFontFamily

@Composable
fun AddressCopyButton(
    address: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E2138),
    textColor: Color = Color.White,
    iconTint: Color = Color.White,
    cornerRadius: Dp = 14.dp,
    onCopied: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = address,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(id = R.drawable.icon_copy),
            contentDescription = "Copy address",
            tint = iconTint,
            modifier = Modifier
                .size(16.dp)
                .clickable {
                    clipboardManager.setText(AnnotatedString(address))
                    onCopied?.invoke()
                }
        )
    }
}


@Preview(
    name = "Address Copy Button – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F1222
)
@Composable
fun AddressCopyButtonPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1222))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "WALLET ADDRESS",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        AddressCopyButton(
            address = "0x1234567890abcdef1234567890abcdef12345678",
            modifier = Modifier.fillMaxWidth(),
            onCopied = { /* no-op for preview */ }
        )
    }
}
