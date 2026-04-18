package com.example.smartcanteen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ModernColorScheme = lightColorScheme(
    primary = CanteenOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFFE65100),
    
    secondary = CanteenGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    
    background = BackgroundLight,
    onBackground = TextPrimary,
    
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextSecondary,
    
    outline = Color(0xFFEEEEEE),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun SmartCanteenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ModernColorScheme,
        typography = Typography,
        content = content
    )
}
