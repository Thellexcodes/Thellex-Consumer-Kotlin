package com.thellex.pay.features.notifications.ui

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thellex.pay.R
import com.thellex.pay.core.decorators.AppGradientBackground
import com.thellex.pay.core.decorators.DarkBlue
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.Midnight
import com.thellex.pay.core.decorators.OutfitFontFamily
import com.thellex.pay.core.decorators.PinkRed
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.formatTransactionTimeHumanReadable
import com.thellex.pay.data.model.BaseSettingsCache
import com.thellex.pay.data.model.BaseSettingsViewModel
import com.thellex.pay.data.model.BaseSettingsViewModelFactory
import com.thellex.pay.data.model.NotificationEntity
import com.thellex.pay.data.model.UserEntity
import com.thellex.pay.data.model.findChainAndAssetIcons
import com.thellex.pay.data.model.toNotificationItem
import com.thellex.pay.features.auth.viewModel.UserViewModel
import com.thellex.pay.features.auth.viewModel.UserViewModelFactory
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.shared.CenteredTopBar
import com.thellex.pay.shared.IconDisplayer
import com.thellex.pay.shared.NotificationFilterRow

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val amount: String,
    val assetSymbol: String,
    val txnId: String?,
    val isConsumed: Boolean,
    val iconUrl: String?
)

enum class NotificationFilter(val label: String) {
    ALL("All"),
    TRANSACTIONS("Transactions"),
    GENERAL_UPDATES("General Updates"),
    MARK("Mark")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(application)
    )

    val authResult by userViewModel.authResult.observeAsState()

    val baseSettingsVM: BaseSettingsViewModel = viewModel(
        factory = BaseSettingsViewModelFactory(context)
    )

    val baseSettings by baseSettingsVM.baseSettings.collectAsState()

    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }

//    val notifications: List<NotificationEntity> =
//        authResult?.notifications ?: emptyList()
    val notificationItems: List<NotificationItem> =
        authResult?.notifications
            ?.map { it.toNotificationItem(baseSettings) }
            ?: emptyList()


    AppGradientBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(PinkRed)
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Midnight)
                    .padding(paddingValues)
            ) {
                CenteredTopBar(
                    title = "Notifications",
                    onBackClick = { navController.popBackStack() }
                )

                NotificationFilterRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notificationItems) { item ->
                        NotificationItemCard(
                            item = item,
                            baseSettings = baseSettings
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: NotificationItem,
    baseSettings: BaseSettingsCache? = null
) {
    val icons = remember(baseSettings, item) {
        baseSettings?.findChainAndAssetIcons(
            network = SupportedBlockchainEnum.matic,
            assetSymbol = item.assetSymbol
        )
    }

    val assetIconUrl = icons?.assetIconUrl

    val cardBackgroundColor =
        if (!item.isConsumed) DarkBlue else Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Text(
            text = item.date.uppercase(),
            color = White,
            fontSize = 18.sp,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(15.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            border = BorderStroke(1.dp, Color(0xFF30363D)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                IconDisplayer(
                    ticker = item.assetSymbol,
                    iconUrl = assetIconUrl,
                    fallbackRes = R.drawable.icon_default_avatar,
                    modifier = Modifier.size(42.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.title.uppercase(),
                            color = Color(0xFF58A6FF),
                            fontSize = 14.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = item.date,
                            color = Color(0xFF58A6FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFFD2A679))) {
                                append(item.message)
                            }
                        },
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationsScreenRoute(
    navController: NavController
){
    NotificationScreen(navController)
}

@Preview
@Composable
fun NotificationItemCardPreview() {
    NotificationItemCard(
        item = NotificationItem(
            id = "1",
            title = "Crypto Deposit",
            message = "Crypto Deposit 1 USDC",
            date = "Today",
            amount = "1.0",
            assetSymbol = "USDC",
            txnId = "abc",
            isConsumed = false,
            iconUrl = ""
        ),
    )
}
