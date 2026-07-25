package com.v2ray.ang.compose

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

/**
 * Original ink-style cityscape, drawn entirely with Canvas primitives.
 * No external image assets — every shape is procedurally generated,
 * so there is nothing here that reproduces anyone else's artwork.
 *
 * Rendered at low alpha so it never competes with foreground text —
 * server addresses, ping, and throughput numbers must stay legible
 * regardless of theme.
 */
@Composable
fun MangaCityscapeBackground(
    modifier: Modifier = Modifier,
    inkColor: Color = InkBlack,
    alpha: Float = 0.05f
) {
    // Relative widths/heights for a simple skyline silhouette —
    // fixed values so the layout is stable across recompositions.
    val buildings = listOf(
        0.06f to 0.30f, 0.05f to 0.55f, 0.08f to 0.20f, 0.04f to 0.42f,
        0.07f to 0.60f, 0.05f to 0.25f, 0.09f to 0.48f, 0.06f to 0.18f,
        0.05f to 0.52f, 0.08f to 0.34f, 0.06f to 0.62f, 0.05f to 0.22f,
        0.07f to 0.44f, 0.04f to 0.28f, 0.08f to 0.50f, 0.05f to 0.16f
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val skylineBaseY = h * 0.62f
        val groundTopY = h * 0.62f

        val ink = inkColor.copy(alpha = alpha)
        val inkFaint = inkColor.copy(alpha = alpha * 0.5f)

        // ── Skyline silhouette ────────────────────────────────────
        var x = 0f
        buildings.forEach { (relWidth, relHeight) ->
            val bw = w * relWidth
            val bh = (h * 0.62f) * relHeight
            drawRect(
                color = ink,
                topLeft = Offset(x, skylineBaseY - bh),
                size = androidx.compose.ui.geometry.Size(bw, bh)
            )
            // A couple of tiny "window" cutouts for texture, drawn as
            // faint lines rather than filled shapes to keep it light.
            val windowRows = (bh / 40f).toInt().coerceIn(0, 5)
            repeat(windowRows) { row ->
                drawLine(
                    color = inkFaint,
                    start = Offset(x + bw * 0.2f, skylineBaseY - bh + 14f + row * 34f),
                    end = Offset(x + bw * 0.8f, skylineBaseY - bh + 14f + row * 34f),
                    strokeWidth = 1.5f
                )
            }
            x += bw + (w * 0.012f)
        }

        // ── Ground / crosswalk perspective lines ──────────────────
        val stripeCount = 7
        val vanishX = w * 0.5f
        for (i in 0 until stripeCount) {
            val t0 = i / stripeCount.toFloat()
            val t1 = (i + 0.55f) / stripeCount.toFloat()
            val path = Path().apply {
                moveTo(w * t0, h)
                lineTo(w * t1, h)
                lineTo(vanishX + (w * t1 - vanishX) * 0.15f, groundTopY)
                lineTo(vanishX + (w * t0 - vanishX) * 0.15f, groundTopY)
                close()
            }
            drawPath(path, color = inkFaint)
        }

        // ── A single streetlamp silhouette, off-center ────────────
        val lampX = w * 0.78f
        val lampBaseY = h
        val lampTopY = groundTopY - h * 0.05f
        drawLine(
            color = ink,
            start = Offset(lampX, lampBaseY),
            end = Offset(lampX, lampTopY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ink,
            start = Offset(lampX, lampTopY),
            end = Offset(lampX + w * 0.08f, lampTopY - h * 0.02f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = ink,
            radius = 6f,
            center = Offset(lampX + w * 0.08f, lampTopY - h * 0.02f)
        )
    }
}
