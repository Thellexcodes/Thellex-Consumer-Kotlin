package com.thellex.pay.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thellex.pay.data.model.ChainInfoDto

@Composable
fun NetworkSelectionContent(
    chains: List<ChainInfoDto>,
    onChainSelected: (ChainInfoDto) -> Unit
) {
    Column {
        chains.forEach { chain ->
            ChainItem(
                chain = chain,
                onClick = { selected -> onChainSelected(selected) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}