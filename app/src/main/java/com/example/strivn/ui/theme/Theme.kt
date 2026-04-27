package com.example.strivn.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val StrivnDarkColorScheme = darkColorScheme(
    primary = StrivnAccent,
    onPrimary = Color.White,

    secondary = StrivnSecondaryAction,
    onSecondary = Color.White,

    background = StrivnBackground,
    onBackground = StrivnTextPrimary,

    surface = StrivnBackground,
    onSurface = StrivnTextPrimary,

    surfaceVariant = StrivnPrimaryCard,
    onSurfaceVariant = StrivnTextSecondary,

    error = StrivnError,
    onError = Color.White,
)

// Optional: keep a light scheme around for previews if needed.
// The app’s design language is dark-first; this is intentionally minimal.
private val StrivnLightColorScheme = lightColorScheme(
    primary = StrivnAccent,
    onPrimary = Color.White,
    secondary = StrivnSecondaryAction,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF0B1220),
    surface = Color.White,
    onSurface = Color(0xFF0B1220),
    error = StrivnError,
    onError = Color.White,
)

@Immutable
data class StrivnComponentDefaults(
    val cardElevationDp: Float = 2f,
    val primaryButtonColors: androidx.compose.material3.ButtonColors,
    val secondaryButtonColors: androidx.compose.material3.ButtonColors,
    val errorButtonColors: androidx.compose.material3.ButtonColors,
)

val LocalStrivnDefaults = staticCompositionLocalOf<StrivnComponentDefaults> {
    error("StrivnComponentDefaults not provided. Wrap your content in STRIVNTheme.")
}

object StrivnDefaults {
    /**
     * Subtle elevation, rounded corners, dashboard-like surfaces.
     */
    @Composable
    fun cardElevation() = CardDefaults.cardElevation(defaultElevation = LocalStrivnDefaults.current.cardElevationDp.dp)

    @Composable
    fun primaryCardColors() = CardDefaults.cardColors(containerColor = StrivnPrimaryCard)

    @Composable
    fun secondaryCardColors() = CardDefaults.cardColors(containerColor = StrivnSecondaryCard)

    @Composable
    fun primaryButtonColors() = LocalStrivnDefaults.current.primaryButtonColors

    @Composable
    fun secondaryButtonColors() = LocalStrivnDefaults.current.secondaryButtonColors

    @Composable
    fun errorButtonColors() = LocalStrivnDefaults.current.errorButtonColors
}

@Composable
fun STRIVNTheme(
    darkTheme: Boolean = true,
    // STRIVN uses an explicit palette; keep dynamic color off by default.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> StrivnDarkColorScheme
        else -> StrivnLightColorScheme
    }

    val componentDefaults = StrivnComponentDefaults(
        cardElevationDp = 2f,
        primaryButtonColors = ButtonDefaults.buttonColors(
            containerColor = StrivnAccent,
            contentColor = Color.White,
            disabledContainerColor = StrivnAccent.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
        secondaryButtonColors = ButtonDefaults.buttonColors(
            containerColor = StrivnSecondaryAction,
            contentColor = Color.White,
            disabledContainerColor = StrivnSecondaryAction.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
        errorButtonColors = ButtonDefaults.buttonColors(
            containerColor = StrivnError,
            contentColor = Color.White,
            disabledContainerColor = StrivnError.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
    )

    androidx.compose.runtime.CompositionLocalProvider(
        LocalStrivnDefaults provides componentDefaults,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = StrivnShapes,
            content = content,
        )
    }
}