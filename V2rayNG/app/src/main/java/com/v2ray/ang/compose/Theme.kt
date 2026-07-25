package com.v2ray.ang.compose

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ─────────────────────────────────────────────────────────────
//  MANGA INK PALETTE  (ported from SandeeVPN)
// ─────────────────────────────────────────────────────────────

val PaperBackground = Color(0xFFF4F1EA)      // Off-white paper
val PaperSurface = Color(0xFFFAF8F5)         // Panel paper
val PaperSurfaceVariant = Color(0xFFEBE7DF)  // Secondary panel

val InkBlack = Color(0xFF121212)
val InkDark = Color(0xFF242424)
val InkHairline = Color(0xFF444444)
val InkMuted = Color(0xFF686868)

// Single spot color — live / active states ONLY
val SpotRed = Color(0xFFE63946)
val SpotRedBg = Color(0xFFFFECEC)

// Inverted ink (night mode = black paper, white ink)
val NightPaper = Color(0xFF101010)
val NightPanel = Color(0xFF181818)
val NightPanelVariant = Color(0xFF222222)
val NightInk = Color(0xFFF0EDE6)
val NightInkMuted = Color(0xFF9A9A9A)

// ─────────────────────────────────────────────────────────────
//  DESIGN TOKENS
// ─────────────────────────────────────────────────────────────

object MangaTokens {
    val ContourThick = androidx.compose.ui.unit.Dp(3f)
    val ContourMedium = androidx.compose.ui.unit.Dp(2f)
    val Hairline = androidx.compose.ui.unit.Dp(1f)

    val GutterSmall = androidx.compose.ui.unit.Dp(6f)
    val GutterMedium = androidx.compose.ui.unit.Dp(12f)
    val GutterLarge = androidx.compose.ui.unit.Dp(18f)

    const val ScreentoneLight = 0.10f
    const val ScreentoneMedium = 0.25f
    const val ScreentoneDense = 0.45f
    const val ScreentoneExtreme = 0.65f
}

// ─────────────────────────────────────────────────────────────
//  COLOR SCHEMES
//  Mapped so v2rayNG's existing screens reskin automatically.
// ─────────────────────────────────────────────────────────────

private val LightColor = lightColorScheme(
    primary = InkBlack,
    onPrimary = PaperSurface,
    primaryContainer = PaperSurfaceVariant,
    onPrimaryContainer = InkBlack,

    secondary = SpotRed,                 // was orange — now the single spot color
    onSecondary = PaperSurface,
    secondaryContainer = SpotRedBg,
    onSecondaryContainer = InkBlack,

    tertiary = InkDark,
    onTertiary = PaperSurface,
    tertiaryContainer = PaperSurfaceVariant,
    onTertiaryContainer = InkBlack,

    error = SpotRed,
    onError = PaperSurface,
    errorContainer = SpotRedBg,
    onErrorContainer = InkBlack,

    background = PaperBackground,
    onBackground = InkBlack,
    surface = PaperSurface,
    onSurface = InkBlack,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkDark,

    outline = InkBlack,                  // ink contour, not grey hairline
    outlineVariant = InkHairline,

    inverseSurface = InkBlack,
    inverseOnSurface = PaperSurface,
    inversePrimary = PaperSurface,
    scrim = InkBlack,
    surfaceTint = Color.Transparent,     // kills M3 elevation tinting

    surfaceContainerLowest = PaperSurface,
    surfaceContainerLow = PaperBackground,
    surfaceContainer = PaperBackground,
    surfaceContainerHigh = PaperSurfaceVariant,
    surfaceContainerHighest = PaperSurfaceVariant,
)

private val DarkColor = darkColorScheme(
    primary = NightInk,
    onPrimary = NightPaper,
    primaryContainer = NightPanelVariant,
    onPrimaryContainer = NightInk,

    secondary = SpotRed,
    onSecondary = NightInk,
    secondaryContainer = Color(0xFF4A1418),
    onSecondaryContainer = NightInk,

    tertiary = NightInkMuted,
    onTertiary = NightPaper,
    tertiaryContainer = NightPanelVariant,
    onTertiaryContainer = NightInk,

    error = SpotRed,
    onError = NightInk,
    errorContainer = Color(0xFF4A1418),
    onErrorContainer = NightInk,

    background = NightPaper,
    onBackground = NightInk,
    surface = NightPanel,
    onSurface = NightInk,
    surfaceVariant = NightPanelVariant,
    onSurfaceVariant = NightInkMuted,

    outline = NightInk,
    outlineVariant = Color(0xFF555555),

    inverseSurface = NightInk,
    inverseOnSurface = NightPaper,
    inversePrimary = NightPaper,
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent,

    surfaceContainerLowest = NightPaper,
    surfaceContainerLow = NightPaper,
    surfaceContainer = NightPanel,
    surfaceContainerHigh = NightPanelVariant,
    surfaceContainerHighest = NightPanelVariant,
)

// ─────────────────────────────────────────────────────────────
//  TYPOGRAPHY
//  Heavy display faces for headings, monospace for all telemetry.
// ─────────────────────────────────────────────────────────────

val MangaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    // Server addresses, ports, UUIDs, latency — always monospace, always legible
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp, letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    ),
)

// ─────────────────────────────────────────────────────────────
//  SEMANTIC COLORS
//  Same names v2rayNG already uses — do not rename.
// ─────────────────────────────────────────────────────────────

val colorPing = InkBlack               // good latency → plain ink
val colorPingRed = SpotRed             // bad latency → spot color
val colorConfigType = InkMuted
val colorFabActive = SpotRed
val colorFabInactiveLight = InkMuted
val colorFabInactiveDark = NightInkMuted
val dividerColorLight = InkBlack
val dividerColorDark = NightInk

// Toast colors
val toastNormalBgLight = Color(0xE6121212)
val toastNormalBgDark = Color(0xE6242424)
val toastSuccessBg = Color(0xE6121212)
val toastErrorBg = Color(0xE6E63946)
val toastInfoBg = Color(0xE6242424)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = PaperSurface

// ─────────────────────────────────────────────────────────────
//  THEME MANAGER — unchanged from upstream
// ─────────────────────────────────────────────────────────────

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColor else LightColor
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MangaTypography          // ← added
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
