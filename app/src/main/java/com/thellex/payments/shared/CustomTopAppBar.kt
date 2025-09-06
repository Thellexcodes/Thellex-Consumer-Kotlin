import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.payments.R
import com.thellex.payments.core.decorators.KumbhSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    backgroundColor: Color = Color(0xFF1A1A2E),
    titleColor: Color = Color.White
) {
    TopAppBar(
        title = {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 80.dp),
                    fontFamily = KumbhSansFontFamily,
                    fontSize = 10.sp,
                )
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                Image(
                    painter = painterResource(id = R.drawable.icon_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(10.dp)
                        .clickable { onBackClick() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = titleColor,
            navigationIconContentColor = titleColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
        scrollBehavior = null
    )
}