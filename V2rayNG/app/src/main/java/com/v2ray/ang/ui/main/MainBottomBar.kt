package com.v2ray.ang.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.compose.InkBlack
import com.v2ray.ang.compose.MangaTokens
import com.v2ray.ang.compose.PaperSurface
import com.v2ray.ang.compose.SpotRed
import com.v2ray.ang.compose.handDrawnInkBorder
import com.v2ray.ang.compose.screentoneBackground
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manga-styled connection dashboard, replacing the thin status bar.
 * Sits above the server list — no navigation changes required.
 *
 * Honesty note: only `isRunning` and `displayText` drive real state.
 * The speed-line burst and idle pulse are decorative transition effects
 * tied to the actual toggle action, not fabricated status indicators.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    isDarkTheme: Boolean,
    onAction: (MainAction) -> Unit
) {
    val burst = remember { Animatable(0f) }
    var wasRunning by remember { mutableStateOf(isRunning) }

    // Fire the ink speed-line burst whenever the connection state flips.
    LaunchedEffect(isRunning) {
        if (isRunning != wasRunning) {
            wasRunning = isRunning
            burst.snapTo(0f)
            burst.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = LinearOutSlowInEasing)
            )
        }
    }

    // Gentle breathing pulse on the core node while connected — signals
    // "alive" without implying any status that isn't real.
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(14.dp)
    ) {
        // ── Layered ink-print shadow — offset flat panel behind ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 5.dp, y = 5.dp)
                .background(InkBlack)
                .height(220.dp)
        )

        // ── Foreground panel ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaperSurface)
                .handDrawnInkBorder(strokeWidth = MangaTokens.ContourThick, color = InkBlack)
                .screentoneBackground(density = MangaTokens.ScreentoneLight, dotColor = InkBlack)
                .padding(18.dp)
        ) {
            // Header row: title + protected/unprotected badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAFE VPN TUNNEL",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = InkBlack
                )
                Box(
                    modifier = Modifier
                        .background(if (isRunning) SpotRed else InkBlack)
                        .handDrawnInkBorder(strokeWidth = MangaTokens.Hairline, color = InkBlack)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isRunning) "PROTECTED" else "UNPROTECTED",
                        color = PaperSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Core node button with ink speed-line burst + idle pulse
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    if (burst.value > 0f) {
                        val progress = burst.value
                        val lineCount = 16
                        val maxLen = size.minDimension * 0.55f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val alpha = (1f - progress).coerceIn(0f, 1f)
                        repeat(lineCount) { i ->
                            val angle = (2 * Math.PI * i / lineCount).toFloat()
                            val len = maxLen * progress
                            val start = Offset(
                                center.x + cos(angle) * (len * 0.4f),
                                center.y + sin(angle) * (len * 0.4f)
                            )
                            val end = Offset(
                                center.x + cos(angle) * len,
                                center.y + sin(angle) * len
                            )
                            drawLine(
                                color = SpotRed.copy(alpha = alpha),
                                start = start,
                                end = end,
                                strokeWidth = 4f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .offset(x = (-3).dp, y = (-3).dp)
                        .background(InkBlack)
                )
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(if (isRunning) SpotRed else InkBlack)
                        .handDrawnInkBorder(
                            strokeWidth = MangaTokens.ContourThick,
                            color = if (isRunning) SpotRed else InkBlack
                        )
                        .combinedClickable(
                            onClick = { onAction(MainAction.ToggleService) },
                            onLongClick = { onAction(MainAction.ToggleService) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp)
                            else painterResource(R.drawable.ic_play_24dp),
                            contentDescription = if (isRunning) "Stop" else "Start",
                            tint = PaperSurface,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "CORE NODE",
                            color = PaperSurface,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "TAP OR LONG-PRESS TO " + if (isRunning) "DISARM" else "ARM TUNNEL",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = InkBlack.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(12.dp))

            // Live status line — real data from the view model
            Text(
                text = displayText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = InkBlack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
