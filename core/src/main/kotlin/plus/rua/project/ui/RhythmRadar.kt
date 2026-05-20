package plus.rua.project.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import plus.rua.project.ShiftKind
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 节奏雷达五维数据。
 */
@Immutable
data class RhythmData(
    val workIntensity: Float,
    val restQuality: Float,
    val holidayMatch: Float,
    val lifeRhythm: Float,
    val mentalBalance: Float
) {
    val overallScore: Float get() = (workIntensity + restQuality + holidayMatch + lifeRhythm + mentalBalance) / 5f

    val scoreLabel: String get() = when {
        overallScore >= 0.8f -> "极佳节奏"
        overallScore >= 0.6f -> "良好节奏"
        overallScore >= 0.4f -> "一般节奏"
        else -> "需要调整"
    }
}

/** 雷达图维度标签。 */
private val RADAR_LABELS = listOf("工作强度", "休息质量", "假日匹配", "生活节奏", "心理平衡")

/**
 * 根据轮班数据计算节奏雷达五维数据。
 */
object RhythmCalculator {

    fun calculate(
        startDate: LocalDate,
        endDate: LocalDate,
        shiftKindAt: (LocalDate) -> ShiftKind?,
        isHoliday: (LocalDate) -> Boolean = { false }
    ): RhythmData {
        val total = startDate.daysUntil(endDate) + 1
        if (total <= 0) return RhythmData(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)

        var workDays = 0
        var offDays = 0
        var consecutiveWorkMax = 0
        var consecutiveWork = 0
        var consecutiveOffMax = 0
        var consecutiveOff = 0
        var holidayMatches = 0
        var totalHolidays = 0

        for (i in 0 until total) {
            val date = startDate.plus(kotlinx.datetime.DatePeriod(days = i))
            val shift = shiftKindAt(date)
            val holiday = isHoliday(date)

            if (holiday) {
                totalHolidays++
                if (shift == ShiftKind.OFF) holidayMatches++
            }

            when (shift) {
                ShiftKind.WORK -> {
                    workDays++
                    consecutiveWork++
                    consecutiveOff = 0
                    consecutiveWorkMax = maxOf(consecutiveWorkMax, consecutiveWork)
                }
                ShiftKind.OFF -> {
                    offDays++
                    consecutiveOff++
                    consecutiveWork = 0
                    consecutiveOffMax = maxOf(consecutiveOffMax, consecutiveOff)
                }
                null -> {}
            }
        }

        val workIntensity = (workDays.toFloat() / total).coerceIn(0f, 1f)
        val restQuality = (offDays.toFloat() / total).coerceIn(0f, 1f)
        val holidayMatch = if (totalHolidays > 0) (holidayMatches.toFloat() / totalHolidays).coerceIn(0f, 1f) else 0.5f
        val rhythmVariation = if (consecutiveWorkMax > 0 && consecutiveOffMax > 0) {
            val ratio = min(consecutiveWorkMax, consecutiveOffMax).toFloat() / maxOf(consecutiveWorkMax, consecutiveOffMax)
            ratio.coerceIn(0f, 1f)
        } else 0.5f
        val mentalBalance = ((restQuality * 0.4f + holidayMatch * 0.3f + rhythmVariation * 0.3f)).coerceIn(0f, 1f)

        return RhythmData(
            workIntensity = workIntensity,
            restQuality = restQuality,
            holidayMatch = holidayMatch,
            lifeRhythm = rhythmVariation,
            mentalBalance = mentalBalance
        )
    }
}

private val RadarCard_shape = RoundedCornerShape(16.dp)

/**
 * 节奏雷达卡片，展示五维雷达图和总分。
 *
 * @param data 五维数据
 * @param modifier 外部布局修饰符
 */
@Composable
fun RhythmRadarCard(
    data: RhythmData,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RadarCard_shape,
        color = colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "节奏雷达", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            RhythmRadar(
                data = data,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(data.overallScore * 100).toInt()}分 · ${data.scoreLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 五维雷达图，Canvas 绘制五边形网格和数据区域。
 *
 * @param data 五维数据（0-1）
 * @param modifier 外部布局修饰符
 */
@Composable
fun RhythmRadar(
    data: RhythmData,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val values = remember(data) { floatArrayOf(data.workIntensity, data.restQuality, data.holidayMatch, data.lifeRhythm, data.mentalBalance) }
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatable.snapTo(0f)
        animatable.animateTo(1f, animationSpec = tween(600))
    }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember { androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = colorScheme.onSurfaceVariant) }
    val sides = 5

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f * 0.7f

        fun angleForSide(i: Int): Double = Math.toRadians(-90.0 + 360.0 * i / sides)
        fun pointForSide(i: Int, radius: Float): Offset {
            val angle = angleForSide(i)
            return Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
        }

        for (level in 1..3) {
            val r = maxRadius * level / 3f
            val path = Path()
            for (i in 0 until sides) {
                val p = pointForSide(i, r)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, colorScheme.outlineVariant.copy(alpha = 0.2f), style = Fill)
            drawPath(path, colorScheme.outlineVariant.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))
        }

        for (i in 0 until sides) {
            val p = pointForSide(i, maxRadius)
            drawLine(colorScheme.outlineVariant.copy(alpha = 0.3f), center, p, strokeWidth = 1.dp.toPx())
        }

        val dataPath = Path()
        val animValues = values.map { it * animatable.value }
        for (i in 0 until sides) {
            val r = maxRadius * animValues[i]
            val p = pointForSide(i, r)
            if (i == 0) dataPath.moveTo(p.x, p.y) else dataPath.lineTo(p.x, p.y)
        }
        dataPath.close()
        drawPath(dataPath, colorScheme.primary.copy(alpha = 0.15f), style = Fill)
        drawPath(dataPath, colorScheme.primary, style = Stroke(width = 2.dp.toPx()))

        for (i in 0 until sides) {
            val r = maxRadius * animValues[i]
            val p = pointForSide(i, r)
            drawCircle(colorScheme.primary, radius = 3.dp.toPx(), center = p)
        }

        for (i in 0 until sides) {
            val labelR = maxRadius + 18.dp.toPx()
            val p = pointForSide(i, labelR)
            val label = RADAR_LABELS[i]
            val layout = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(p.x - layout.size.width / 2f, p.y - layout.size.height / 2f)
            )
        }
    }
}
