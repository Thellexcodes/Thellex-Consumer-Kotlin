package com.thellex.payments.features.admin

import CustomTopAppBar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.thellex.payments.core.decorators.BrightSkyBlue
import com.thellex.payments.core.decorators.GoldenYellow
import com.thellex.payments.core.decorators.KumbhSansFontFamily
import com.thellex.payments.core.decorators.Midnight
import com.thellex.payments.core.decorators.OutfitFontFamily
import com.thellex.payments.core.decorators.White
import com.thellex.payments.core.utils.Helpers.disableDecorFitsSystemWindows
import com.thellex.payments.core.utils.Helpers.setTransparentStatusBarWithWhiteIcons
import com.thellex.payments.features.auth.viewModel.UserRepository
import com.thellex.payments.network.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// -------------------- Data Model --------------------
data class RevenueItem(
    val title: String,
    val total: String,
    val titleColor: Color = White,
    val backgroundColor: Color = Midnight,
    val descColor: Color = Midnight
)

// -------------------- Revenue Item Composable --------------------
@Composable
fun RevenueItemBox(item: RevenueItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(179.dp)
            .background(item.backgroundColor, shape = RoundedCornerShape(9.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = item.total,
                color = item.titleColor,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Text(
                text = item.title,
                color = item.descColor,
                fontFamily = KumbhSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// -------------------- Screen Composable --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRevenueScreen(
    revenueData: List<RevenueItem>,
    isLoading: Boolean = false,
    onBackClick: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CustomTopAppBar(
                    title = "REVENUE",
                    onBackClick = onBackClick,
                    backgroundColor = Midnight,
                    titleColor = White
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Midnight)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    revenueData.forEach { item -> RevenueItemBox(item) }
                }
            }
        )

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = White,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

// -------------------- Activity --------------------
class RevenueActivity : ComponentActivity() {
    private val userRepository by lazy { UserRepository.getInstance(applicationContext) }
    private val TAG = "RevenueActivity1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disableDecorFitsSystemWindows()
        setTransparentStatusBarWithWhiteIcons()

        val revenueItems = mutableStateOf<List<RevenueItem>>(emptyList())
        val isLoading = mutableStateOf(true)

        lifecycleScope.launch {
            try {
                val authToken = userRepository.getToken().first()
                    ?: throw IllegalStateException("No auth token available")

                val response = withContext(Dispatchers.IO) {
                    ApiClient.getAuthenticatedAdminApi(this@RevenueActivity, authToken).fetchRevenues()
                }

                val result = response.result

                result?.let {
                    val items = listOf(
//                        RevenueItem(
//                            title = it.totalRevenue!!.title.uppercase(),
//                            total = it.totalRevenue.total,
//                            titleColor = White,
//                            backgroundColor = DarkBlue,
//                            descColor = SteelBlueGrey
//                        ),
                        RevenueItem(
                            title = it.fiatRevenue.title.uppercase(),
                            total = it.fiatRevenue.total,
                            titleColor = White,
                            backgroundColor = GoldenYellow,
                            descColor = Midnight
                        ),
                        RevenueItem(
                            title = it.cryptoRevenue.title.uppercase(),
                            total = it.cryptoRevenue.total,
                            titleColor = White,
                            backgroundColor = BrightSkyBlue,
                            descColor = Midnight
                        )
                    )
                    revenueItems.value = items
                }
            } catch (e: Exception) {
                Log.e(TAG, "API call failed", e)
            } finally {
                isLoading.value = false
            }
        }

        setContent {
            MaterialTheme {
                AllRevenueScreen(
                    revenueData = revenueItems.value,
                    isLoading = isLoading.value,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

// -------------------- Preview --------------------
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewAllRevenueScreen() {
    val previewData = listOf(
//        RevenueItem(title = "TOTAL REVENUE", total = "8,000 USD", White, DarkBlue, SteelBlueGrey),
        RevenueItem(title = "FIAT REVENUE", total = "50,000 NGN", White, GoldenYellow, Midnight),
        RevenueItem(title = "CRYPTO REVENUE", total = "100,000 USD", White, BrightSkyBlue, Midnight)
    )

    MaterialTheme {
        AllRevenueScreen(
            revenueData = previewData,
            isLoading = true,
            onBackClick = {}
        )
    }
}
