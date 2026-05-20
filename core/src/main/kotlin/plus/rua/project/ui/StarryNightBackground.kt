package plus.rua.project.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Immutable
private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Float,
    val twinkleSpeed: Float,
    val phaseOffset: Float
)

@Immutable
private data class ShootingStar(
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val length: Float,
    val progress: Float
)

private fun generateStars(count: Int, seed: Long = 42): List<Star> {
    val random = Random(seed)
    return (0 until count).map {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            size = random.nextFloat() * 2f + 0.5f,
            brightness = random.nextFloat() * 0.5f + 0.3f,
            twinkleSpeed = random.nextFloat() * 0.5f + 0.3f,
            phaseOffset = random.nextFloat() * 6.28f
        )
    }
}

/**
 * 星夜背景，暗色模式下渲染闪烁星空粒子。
 *
 * 在浅色模式下不渲染任何内容（透明），仅在暗色模式下显示。
 * 包含约 100 颗闪烁的星星和偶现的流星效果。
 *
 * @param modifier 外部布局修饰符
 */
@Composable
fun StarryNightBackground(
    modifier: Modifier = Modifier
) {
    if (!isSystemInDarkTheme()) return

    val stars = remember { generateStars(100) }
    val infiniteTransition = rememberInfiniteTransition(label = "starryNight")

    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinklePhase"
    )

    val shootingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shootingStar"
    )

    val backgroundColor1 = Color(0xFF0D1117)
    val backgroundColor2 = Color(0xFF161B22)
    val starColor = Color.White

    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(colors = listOf(backgroundColor1, backgroundColor2)))

        stars.forEach { star ->
            val twinkle = (sin(twinklePhase * star.twinkleSpeed + star.phaseOffset) + 1f) / 2f
            val alpha = star.brightness * (0.4f + twinkle * 0.6f)
            val radius = (star.size * (0.8f + twinkle * 0.2f)).dp.toPx()
            drawCircle(
                color = starColor.copy(alpha = alpha),
                radius = radius,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }

        val shootingIndex = (shootingProgress * 5f).toInt()
        val shootingVisible = shootingProgress * 5f - shootingIndex in 0.7f..0.95f
        if (shootingVisible) {
            val random = Random(shootingIndex * 12345L)
            val sx = random.nextFloat() * size.width * 0.6f + size.width * 0.2f
            val sy = random.nextFloat() * size.height * 0.3f
            val angle = Math.toRadians(30.0 + random.nextFloat() * 30.0)
            val len = 60.dp.toPx()
            val localProgress = (shootingProgress * 5f - shootingIndex - 0.7f) / 0.25f

            val endX = sx + len * cos(angle).toFloat() * localProgress
            val endY = sy + len * sin(angle).toFloat() * localProgress
            val tailX = endX - 20.dp.toPx() * cos(angle).toFloat()
            val tailY = endY - 20.dp.toPx() * sin(angle).toFloat()

            drawLine(
                color = starColor.copy(alpha = 0.6f * (1f - localProgress)),
                start = Offset(tailX, tailY),
                end = Offset(endX, endY),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
