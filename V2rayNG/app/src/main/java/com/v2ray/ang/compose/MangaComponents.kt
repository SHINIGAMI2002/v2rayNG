package com.v2ray.ang.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paper grain overlay — subtle organic noise across surfaces.
 * Deterministic seed so it does not shimmer between recompositions.
 */
fun Modifier.paperGrainOverlay(
    enabled: Boolean = true,
    opacity: Float = 0.04f,
    grainDensityDp: Dp = 12.dp,
    inkColor: Color = InkBlack
): Modifier = if (!enabled || opacity <= 0f) this else this.drawWithContent {
    drawContent()

    val spacingPx = grainDensityDp.toPx()
    val noiseColor = inkColor.copy(alpha = opacity)
    var y = spacingPx / 2f
    var seed = 1337

    while (y < size.height) {
        var x = spacingPx / 2f
        while (x < size.width) {
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val jitterX = ((seed % 7) - 3) * 0.8f
            val jitterY = (((seed / 7) % 7) - 3) * 0.8f
            val dotRadius = if (seed % 3 == 0) 1.2f else 0.7f
            drawCircle(noiseColor, dotRadius, Offset(x + jitterX, y + jitterY))
            x += spacingPx
        }
        y += spacingPx
    }
}

/**
 * Hand-drawn ink border — corner wobble so nothing reads as machine-perfect vector.
 */
fun Modifier.handDrawnInkBorder(
    enabled: Boolean = true,
    strokeWidth: Dp = MangaTokens.ContourMedium,
    color: Color = InkBlack
): Modifier = if (!enabled) {
    this.border(strokeWidth, color, RoundedCornerShape(0.dp))
} else this.drawBehind {
    val swPx = strokeWidth.toPx()
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(1f, 2f)
        lineTo(w - 2f, -1f)
        lineTo(w + 1f, h - 1f)
        lineTo(-1f, h + 1f)
        close()
    }
    drawPath(path, color, style = Stroke(width = swPx + 0.5f, cap = StrokeCap.Round))
}

/**
 * Misaligned spot-color print — mimics off-register press alignment.
 */
fun Modifier.misalignedSpotColor(
    enabled: Boolean = true,
    offsetDp: Dp = 2.dp,
    spotColor: Color = SpotRed
): Modifier = if (!enabled) this else this.drawBehind {
    val offsetPx = offsetDp.toPx()
    drawRect(
        color = spotColor.copy(alpha = 0.85f),
        topLeft = Offset(offsetPx, offsetPx * 0.6f),
        size = Size(size.width, size.height)
    )
}

/**
 * Halftone screentone — hexagonal dot grid. Density expresses depth / load / intensity.
 */
fun Modifier.screentoneBackground(
    density: Float = MangaTokens.ScreentoneLight,
    dotColor: Color = InkBlack,
    spacingDp: Dp = 10.dp
): Modifier = this.drawBehind {
    if (density <= 0f) return@drawBehind

    val spacingPx = spacingDp.toPx()
    val radiusPx = (spacingPx * 0.42f) * density.coerceIn(0.05f, 0.9f)

    var y = spacingPx / 2f
    var row = 0
    while (y < size.height) {
        val offsetX = if (row % 2 == 1) spacingPx / 2f else 0f
        var x = spacingPx / 2f + offsetX
        while (x < size.width) {
            drawCircle(dotColor, radiusPx, Offset(x, y))
            x += spacingPx
        }
        y += spacingPx * 0.866f
        row++
    }
}

/**
 * Manga panel — thick contour, paper fill, optional screentone and grain.
 */
@Composable
fun MangaPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PaperSurface,
    borderColor: Color = InkBlack,
    borderWidth: Dp = MangaTokens.ContourThick,
    screentoneDensity: Float = 0f,
    activeSpotAccent: Boolean = false,
    paperGrainEnabled: Boolean = true,
    handDrawnBordersEnabled: Boolean = true,
    misalignedSpotEnabled: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val finalBorderColor = if (activeSpotAccent) SpotRed else borderColor
    val finalBorderWidth = if (activeSpotAccent) borderWidth + 1.dp else borderWidth

    Box(
        modifier = modifier
            .misalignedSpotColor(enabled = misalignedSpotEnabled && activeSpotAccent)
            .background(backgroundColor)
            .handDrawnInkBorder(handDrawnBordersEnabled, finalBorderWidth, finalBorderColor)
            .screentoneBackground(screentoneDensity, InkBlack.copy(alpha = 0.25f))
            .paperGrainOverlay(paperGrainEnabled)
            .padding(MangaTokens.GutterMedium),
        content = content
    )
}

/**
 * Vertical Japanese-style accent text. Decorative only — never for critical info.
 */
@Composable
fun VerticalMangaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = InkMuted,
    fontSize: Int = 11
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        text.forEach { char ->
            Text(
                text = char.toString(),
                color = color,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (fontSize + 2).sp
            )
        }
    }
}

/**
 * Manga badge / speech box.
 */
@Composable
fun MangaBadge(
    text: String,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isActive) SpotRed else InkBlack)
            .border(1.dp, InkBlack, RoundedCornerShape(0.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = PaperSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Manga action button — sharp ink borders, spot color when active.
 */
@Composable
fun MangaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isSecondary: Boolean = false,
    isSpotActive: Boolean = false,
    enabled: Boolean = true
) {
    val actualPrimary = if (isSecondary) false else isPrimary

    val bgColor = when {
        !enabled -> PaperSurface
        isSpotActive -> SpotRed
        actualPrimary -> InkBlack
        else -> PaperSurface
    }
    val textColor = when {
        !enabled -> InkMuted
        isSpotActive -> PaperSurface
        actualPrimary -> PaperSurface
        else -> InkBlack
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(bgColor)
            .border(
                width = if (isSpotActive) MangaTokens.ContourThick else MangaTokens.ContourMedium,
                color = if (isSpotActive) SpotRed else InkBlack,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}
