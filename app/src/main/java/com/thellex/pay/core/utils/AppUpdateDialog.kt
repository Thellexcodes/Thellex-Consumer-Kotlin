package com.thellex.pay.core.utils

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thellex.pay.R
import com.thellex.pay.core.decorators.GoldenYellow
import com.thellex.pay.core.decorators.KumbhSansFontFamily
import com.thellex.pay.core.decorators.SteelBlueGrey
import com.thellex.pay.core.decorators.Transparent
import com.thellex.pay.core.decorators.White
import com.thellex.pay.core.utils.Helpers.capitalizeFirst
import org.w3c.dom.Text

@Composable
fun AppUpdateScreen(
    latestVersion: String,
    downloadUrl: String?,
    updateType: String,
    releaseNotes: String? = null
) {
    val context = LocalContext.current
    val releaseNotesList = releaseNotes
        ?.lines()
        ?.map { it.trimStart('-', ' ') }
        ?.filter { it.isNotBlank() }
        ?: emptyList()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top half: image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_update),
                    contentDescription = "Update Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Middle content: title and description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "New Update Available",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = KumbhSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Update announcement
                    Text(
                        text = "A $updateType update (v$latestVersion) is now available.",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Explanation / additional info
                    Text(
                        text = "This current version is no longer supported. " +
                                "We apologize for any inconvenience this may have caused.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = KumbhSansFontFamily,
                            color = SteelBlueGrey,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .verticalScroll(scrollState)
                ) {
                    releaseNotesList.forEach { note ->
                        Text(
                            text = "• ${note.capitalizeFirst()}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = KumbhSansFontFamily,
                                color = SteelBlueGrey,
                                lineHeight = 20.sp,
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            // Bottom button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        downloadUrl?.let {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                            context.startActivity(intent)
                        }
                        System.exit(0)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenYellow
                    )
                ) {
                    Text(
                        text = "Update Now",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = KumbhSansFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(32.dp),
//            verticalArrangement = Arrangement.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//

//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "A $updateType update (v$latestVersion) is available.",
//                style = MaterialTheme.typography.bodyLarge,
//                textAlign = TextAlign.Center
//            )
//