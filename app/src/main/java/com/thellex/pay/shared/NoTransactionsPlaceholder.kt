package com.thellex.pay.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.White

@Composable
fun NoTransactionsPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Illustration
        Image(
            painter = painterResource(id = R.drawable.img_empty_list),
            contentDescription = null,
            modifier = Modifier.size(94.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NO TRANSACTIONS",
            color = White,
            fontSize = 16.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start transacting today to see your records.",
            color = SteelBlueGrey,
            fontSize = 10.sp,
            fontFamily = KumbhSansFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
fun NoTransactionsPlaceholderPreview() {
    NoTransactionsPlaceholder()
}
