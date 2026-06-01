package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val DarkColorScheme =
  darkColorScheme(
    primary = SlateAccentLight,
    secondary = SlateCard,
    background = SlateDark,
    surface = SlateCard,
    onPrimary = SlateDark,
    onSecondary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SlateAccent,
    secondary = SlateLightCard,
    background = SlateLightBg,
    surface = SlateLightCard,
    onPrimary = Color.White,
    onSecondary = TextPrimaryLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
  )

fun getPrimaryColorForPreset(preset: String): Color {
    return when (preset) {
        "الافتراضي" -> Color(0xFF4F46E5) // Indigo
        "كاتبوتشينو" -> Color(0xFFD4A5C9) // Mocha rose/lavender
        "أخضر" -> Color(0xFF2E7D32) // Forest green
        "أرجواني" -> Color(0xFF9C27B0) // Deep purple
        "منتصف الليل" -> Color(0xFFE91E63) // Crimson pink
        "Nord" -> Color(0xFF5E81AC) // Nord blue
        "أحمر" -> Color(0xFFD32F2F) // Red
        "تاكو" -> Color(0xFF8D6E63) // Taco Brown
        "ازرق مخضر" -> Color(0xFF00796B) // Teal
        "ازرق مخضر و فيروز" -> Color(0xFF0097A7) // Turquoise
        "موجة مد و جزر" -> Color(0xFF1565C0) // Ocean blue
        "ين & يانغ" -> Color(0xFF757575) // Gray/Yin & Yang
        "يوتسوبا" -> Color(0xFFF57C00) // Yotsuba Orange
        else -> Color(0xFFE91E63) // Default/Midnight accent
    }
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  pureBlackDark: Boolean = false,
  themePreset: String = "منتصف الليل",
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val accentColor = getPrimaryColorForPreset(themePreset)
  val backgroundColor = if (darkTheme) {
      if (pureBlackDark) Color.Black else Color(0xFF12141C)
  } else {
      Color(0xFFF8FAFC)
  }
  val cardColor = if (darkTheme) {
      if (pureBlackDark) Color(0xFF0F0F12) else Color(0xFF1E2230)
  } else {
      Color(0xFFFFFFFF)
  }

  val colorScheme = if (darkTheme) {
      darkColorScheme(
          primary = accentColor,
          secondary = cardColor,
          background = backgroundColor,
          surface = cardColor,
          onPrimary = Color.Black,
          onSecondary = TextPrimaryDark,
          onBackground = TextPrimaryDark,
          onSurface = TextPrimaryDark,
      )
  } else {
      lightColorScheme(
          primary = accentColor,
          secondary = cardColor,
          background = backgroundColor,
          surface = cardColor,
          onPrimary = Color.White,
          onSecondary = TextPrimaryLight,
          onBackground = TextPrimaryLight,
          onSurface = TextPrimaryLight,
      )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

