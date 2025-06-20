package com.example.geopatitas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
*/
// NUEVO

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnSecondary,

    background = BackgroundLight,
    onBackground = OnBackground,

    surface = SurfaceLight,
    onSurface = OnSurface,

    error = Error,
    onError = OnError
)

private val DarkColors = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,

    secondary = SecondaryDark,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnSecondary,

    background = BackgroundDark,
    onBackground = OnBackground,

    surface = SurfaceDark,
    onSurface = OnSurface,

    error = Error,
    onError = OnError
)

@Composable
fun GeoPatitasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography
    ) {
        AppBackground(imageUrl = "https://i.pinimg.com/736x/9b/e4/94/9be4946bf3984d53623333eefc6572f8.jpg")  {
            content()
        }
    }
}

/* OLD THEME
@Composable
fun GeoPatitasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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

*/