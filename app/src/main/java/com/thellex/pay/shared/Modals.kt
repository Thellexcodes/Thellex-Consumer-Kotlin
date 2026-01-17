package com.thellex.pay.shared

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.data.model.DepositTokenDto
import com.thellex.pay.data.model.TokenInfo

@Composable
fun CryptoTokenSelectionContent(
    tokens: List<TokenInfo>,
    selectedSymbol: String?,
    chainName: String = "",
    onSelected: (TokenInfo) -> Unit
) {
    Log.d("Base", "list are $tokens")
    Column(modifier = Modifier.fillMaxWidth()) {
        // Optional: show chain name at top
        if (chainName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tokens on $chainName",
                    color = SteelBlueGrey,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Divider(color = DarkBlue, thickness = 1.dp)
        }

        if (tokens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No supported tokens on this network",
                    color = SteelBlueGrey,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tokens) { token ->
                    TokenItem(
                        token = token,
                        selected = token.symbol.name.equals(selectedSymbol, ignoreCase = true),
                        onClick = { onSelected(token) }
                    )
                    Spacer(
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FiatSelectionContent(
    fiats: List<DepositTokenDto>,
    selectedTicker: String?,
    onSelected: (DepositTokenDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (fiats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No fiat options available",
                    color = SteelBlueGrey,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn {
                items(fiats) { fiat ->
                    FiatTokenItem(
                        name = fiat.name,
                        symbol = fiat.ticker.uppercase(),
                        iconUrl = fiat.iconUrl,
                        isSelected = fiat.ticker.equals(selectedTicker, ignoreCase = true),
                        onClick = { onSelected(fiat) }
                    )
                    Divider(
                        color = DarkBlue.copy(alpha = 0.6f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}