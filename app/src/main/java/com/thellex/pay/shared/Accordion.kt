package com.thellex.pay.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkPurple
import com.thellex.pay.core.decorators.DeepNavy
import com.thellex.pay.core.decorators.Midnight

@Composable
fun Accordion(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    backgroundColor: Color = DeepNavy,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White
            )
        }

        // Body
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF14132C)
@Composable
fun AccordionPreview_Simple() {
    Accordion(
        title = "Transaction Details"
    ) {
        Text(
            text = "Network: Ethereum",
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fee: $0.12",
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF14132C)
@Composable
fun AccordionPreview_Expanded() {
    Accordion(
        title = "Summary",
        initiallyExpanded = true
    ) {
        Text(
            text = "Amount: 1 USDT",
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Network Fee: $0.05",
            color = Color.Gray
        )
    }
}
