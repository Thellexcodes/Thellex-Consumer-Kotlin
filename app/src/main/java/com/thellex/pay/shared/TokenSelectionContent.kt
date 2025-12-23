package com.thellex.pay.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thellex.pay.data.model.TokenInfo

@Composable
fun TokenSelectionContent(
    tokens: List<TokenInfo>,
    selectedTokenId: String?,
    onTokenSelected: (TokenInfo) -> Unit
) {
    Column {
        tokens.forEach { token ->
            TokenItem(
                token = token,
                selected = token.name == selectedTokenId,
                onClick = onTokenSelected
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}