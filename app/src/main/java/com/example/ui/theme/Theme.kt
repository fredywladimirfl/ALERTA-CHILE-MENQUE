package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = ChileBlueLight,
  onPrimary = Color.White,
  primaryContainer = ChileBlueDark,
  onPrimaryContainer = Color(0xFFDCE2F9),
  secondary = AlertWarningOrange,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF4A2800),
  onSecondaryContainer = Color(0xFFFFDCC1),
  tertiary = AlertSafeGreen,
  onTertiary = Color.White,
  error = AlertEmergencyRed,
  onError = Color.White,
  background = BackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceCardDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = SurfaceBorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = ChileBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD6E4FF),
  onPrimaryContainer = ChileBlueDark,
  secondary = ChileRed,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFDAD6),
  onSecondaryContainer = ChileRedDark,
  tertiary = AlertSafeGreen,
  onTertiary = Color.White,
  error = AlertEmergencyRed,
  onError = Color.White,
  background = BackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceCardLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = SurfaceBorderLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to dark emergency theme for tactical readability and battery preservation
  dynamicColor: Boolean = false, // Keep authoritative Chile Emergency branding
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
