import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thellex.payments.R
import com.thellex.payments.core.decorators.KumbhSansFontFamily

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null, // keep callback
    navController: NavController? = null, // optional navController fallback
    backgroundColor: Color = Color(0xFF1A1A2E),
    titleColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val activity = LocalContext.current as? Activity

    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        color = titleColor,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null || navController != null) {
                IconButton(
                    onClick = {
                        // Call the provided callback first
                        onBackClick?.invoke()

                        // Try to pop Compose nav stack
                        val popped = navController?.popBackStack() ?: false

                        // If no Compose back stack and no callback handled it, finish activity
                        if (!popped && onBackClick == null) {
                            activity?.finish()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_back),
                        contentDescription = "Back",
                        tint = titleColor
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = titleColor,
            navigationIconContentColor = titleColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(contentPadding),
        scrollBehavior = null
    )
}