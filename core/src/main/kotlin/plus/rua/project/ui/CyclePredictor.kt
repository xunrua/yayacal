package plus.rua.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 周期预测结果。
 */
@Immutable
data class CyclePredictionResult(
    val markerName: String,
    val averageCycleDays: Float,
    val standardDeviation: Float,
    val confidence: CycleConfidence,
    val predictedDates: List<LocalDate>,
    val pastDates: List<LocalDate>
)

/** 预测置信度。 */
enum class CycleConfidence(val label: String) {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低")
}

/**
 * 周期预测器，基于历史日期标记预测未来出现时间。
 */
object CyclePredictor {

    fun predict(
        markerName: String,
        pastDates: List<LocalDate>,
        today: LocalDate,
        predictCount: Int = 3
    ): CyclePredictionResult? {
        if (pastDates.size < 2) return null

        val sorted = pastDates.sorted()
        val intervals = (1 until sorted.size).map { i ->
            sorted[i - 1].daysUntil(sorted[i])
        }

        val avgCycle = intervals.average().toFloat()
        val stdDev = if (intervals.size > 1) {
            val variance = intervals.map { (it - avgCycle).pow(2) }.average()
            sqrt(variance).toFloat()
        } else 0f

        val confidence = when {
            stdDev < avgCycle * 0.1f -> CycleConfidence.HIGH
            stdDev < avgCycle * 0.25f -> CycleConfidence.MEDIUM
            else -> CycleConfidence.LOW
        }

        val lastDate = sorted.last()
        val predicted = (1..predictCount).map { i ->
            lastDate.plus(kotlinx.datetime.DatePeriod(days = (avgCycle * i).toInt()))
        }

        return CyclePredictionResult(
            markerName = markerName,
            averageCycleDays = avgCycle,
            standardDeviation = stdDev,
            confidence = confidence,
            predictedDates = predicted,
            pastDates = sorted
        )
    }
}

private val PredictionCard_shape = RoundedCornerShape(16.dp)

/**
 * 周期预测卡片，展示历史标记和未来预测。
 *
 * @param result 预测结果
 * @param today 今天日期
 * @param modifier 外部布局修饰符
 */
@Composable
fun CyclePredictionCard(
    result: CyclePredictionResult,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val confidenceColor = when (result.confidence) {
        CycleConfidence.HIGH -> colorScheme.primary
        CycleConfidence.MEDIUM -> colorScheme.tertiary
        CycleConfidence.LOW -> colorScheme.error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PredictionCard_shape,
        color = colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = result.markerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "平均周期",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result.averageCycleDays.toInt()} 天",
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "置信度",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(text = result.confidence.label, fontWeight = FontWeight.Bold, color = confidenceColor)
                }
                if (result.predictedDates.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "下次预测",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        val nextDate = result.predictedDates.first()
                        val daysUntil = today.daysUntil(nextDate)
                        Text(
                            text = "${daysUntil}天后",
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            CycleTimeline(
                pastDates = result.pastDates.takeLast(6),
                predictedDates = result.predictedDates.take(3),
                today = today,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
        }
    }
}

/**
 * 周期时间线，可视化历史标记和预测事件。
 *
 * @param pastDates 历史标记日期
 * @param predictedDates 预测日期
 * @param today 今天
 * @param modifier 外部布局修饰符
 */
@Composable
fun CycleTimeline(
    pastDates: List<LocalDate>,
    predictedDates: List<LocalDate>,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val allDates = pastDates + listOf(today) + predictedDates
    val minDate = allDates.min()
    val maxDate = allDates.max()
    val totalSpan = minDate.daysUntil(maxDate).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val lineY = size.height / 2f
        val padding = 8.dp.toPx()

        drawLine(
            color = colorScheme.outlineVariant,
            start = Offset(padding, lineY),
            end = Offset(size.width - padding, lineY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )

        fun dateToX(date: LocalDate): Float {
            val offset = minDate.daysUntil(date)
            return padding + (size.width - padding * 2) * (offset.toFloat() / totalSpan)
        }

        val todayX = dateToX(today)
        drawLine(
            color = colorScheme.primary.copy(alpha = 0.3f),
            start = Offset(todayX, 0f),
            end = Offset(todayX, size.height),
            strokeWidth = 1.dp.toPx()
        )

        pastDates.forEach { date ->
            val x = dateToX(date)
            drawCircle(
                color = colorScheme.primary,
                radius = 5.dp.toPx(),
                center = Offset(x, lineY),
                style = Fill
            )
        }

        predictedDates.forEach { date ->
            val x = dateToX(date)
            drawCircle(
                color = colorScheme.primary,
                radius = 5.dp.toPx(),
                center = Offset(x, lineY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        drawCircle(
            color = colorScheme.error.copy(alpha = 0.5f),
            radius = 3.dp.toPx(),
            center = Offset(todayX, lineY)
        )
    }
}
