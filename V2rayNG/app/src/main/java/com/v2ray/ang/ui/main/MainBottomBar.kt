package com.v2ray.ang.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointerInput
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
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manga-styled connection dashboard, replacing the thin status bar.
 * Sits above the server list — no navigation changes required.
 *
 * Honesty note: only `isRunning` and `displayText` drive real state.
 * The speed-line burst is a decorative transition effect tied to the
 * actual toggle action, not a fabricated status indicator.
 */
@Composable
fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    isDarkTheme: Boolean,
    onAction: (MainAction) -> Unit
) {
    val scope = rememberCoroutineScope()
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(PaperSurface)
            .handDrawnInkBorder(strokeWidth = MangaTokens.ContourThick, color = InkBlack)
            .padding(16.dp)
    ) {
        // ── Header row: title + protected/unprotected badge ──────
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

        androidx.compose.foundation.layout.Spacer(Modifier.height(14.dp))

        // ── Core node button with ink speed-line burst ────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(180.dp)
            ) {
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
                    .size(112.dp)
                    .background(if (isRunning) SpotRed else InkBlack)
                    .handDrawnInkBorder(
                        strokeWidth = MangaTokens.ContourThick,
                        color = if (isRunning) SpotRed else InkBlack
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onAction(MainAction.ToggleService) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isRunning) painterResource(R.drawable.ic_stop_24dp)
                    else painterResource(R.drawable.ic_play_24dp),
                    contentDescription = if (isRunning) "Stop" else "Start",
                    tint = PaperSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))

        Text(
            text = "LONG-PRESS TO " + if (isRunning) "DISARM" else "ARM TUNNEL",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = InkBlack.copy(alpha = 0.55f),
            modifier = Modifier.padding(top = 2.dp)
        )

        androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))

        // ── Live status line — real data from the view model ─────
        Text(
            text = displayText,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = InkBlack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
