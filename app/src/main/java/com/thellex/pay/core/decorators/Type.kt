package com.thellex.pay.core.decorators

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.thellex.pay.R
import androidx.compose.material3.Typography

val OutfitFontFamily = FontFamily(
    Font(resId = R.font.outfit_regular, weight = FontWeight.Normal),
    Font(resId = R.font.outfit_bold, weight = FontWeight.Bold),
    Font(resId = R.font.outfit_light, weight = FontWeight.Light),
    Font(resId = R.font.outfit_medium, weight = FontWeight.Medium),
    Font(resId = R.font.outfit_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.outfit_thin, weight = FontWeight.Thin),
    Font(resId = R.font.outfit_extralight, weight = FontWeight.ExtraLight),
    Font(resId = R.font.outfit_extrabold, weight = FontWeight.ExtraBold),
    Font(resId = R.font.outfit_black, weight = FontWeight.Black)
)

val KumbhSansFontFamily = FontFamily(
    Font(resId = R.font.kumbhsans_regular, weight = FontWeight.Normal),
    Font(resId = R.font.kumbhsans_bold, weight = FontWeight.Bold),
    Font(resId = R.font.kumbhsans_light, weight = FontWeight.Light),
    Font(resId = R.font.kumbhsans_medium, weight = FontWeight.Medium),
    Font(resId = R.font.kumbhsans_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.kumbhsans_thin, weight = FontWeight.Thin),
    Font(resId = R.font.kumbhsans_extralight, weight = FontWeight.ExtraLight),
    Font(resId = R.font.kumbhsans_extrabold, weight = FontWeight.ExtraBold),
    Font(resId = R.font.kumbhsans_black, weight = FontWeight.Black)
)

// Optionally, update the Typography to use these fonts globally
val Typography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = OutfitFontFamily),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = KumbhSansFontFamily)
    // Customize other text styles as needed
)