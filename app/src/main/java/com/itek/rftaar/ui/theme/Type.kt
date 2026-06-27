package com.itek.rftaar.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.itek.rftaar.R

// Set of Material typography styles to start with

val Jakarta = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),

    )

data class GarudaTypography(
    val bodyLarge: TextStyle,
    val headingH1: TextStyle,
    val textSubtext: TextStyle,
    val buttonSemiBold: TextStyle,
    val smallTxt: TextStyle,
    val smallTxtBold: TextStyle,
    val textSemiBold: TextStyle,
    val noteText: TextStyle,
    val textMedium: TextStyle,
    val bigFont: TextStyle,
    val bigBoldFont : TextStyle,
    val mediumText: TextStyle,
    val countText: TextStyle,
    val dashobardCount: TextStyle,
)
val Typography = GarudaTypography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headingH1 = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = BlackColor
    ),
    textSubtext = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextSubtext
    ),
    buttonSemiBold = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = WhiteColor
    ),
    smallTxt = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        color = TextSubtext
    ),
    smallTxtBold = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        color = TextSubtext
    ),
    textSemiBold = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = TextBlack
    ),
     noteText = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = LightGray
    ),
     textMedium = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = LightGray
    ),
    bigFont = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        color = BlackColor,
        textAlign = TextAlign.Center
    ),
    bigBoldFont = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 28.sp,
        color = BlackColor
    ),
    mediumText = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = BlackColor
    ),
    countText = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        color = ShadedYellow,
        textAlign = TextAlign.Center
    ),
    dashobardCount = TextStyle(
        fontFamily = Jakarta,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 28.sp,
        color = TextSubtext,
        textAlign = TextAlign.Center
    )



)