package plus.rua.project.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import plus.rua.project.ShiftKind
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 饼图分段数据。
 */
@Immutable
data class PieSegment(
    val label: String,
    val value: Float,
    val color: Color,
    val children: List<PieSegment>? = null
)

/**
 * 时间分布数据。
 */
@Immutable
data class TimePieChartData(
    val segments: List<PieSegment>,
    val title: String = ""
) {
    val total: Float get() = segments.sumOf { it.value.toDouble() }.toFloat()
}

/**
 * 时间分布统计工具。
 */
object TimeDistribution {

    fun monthDistribution(
        year: Int,
        month: Int,
        shiftKindAt: (LocalDate) -> ShiftKind?
    ): TimePieChartData {
        @Suppress("DEPRECATION")
        val start = LocalDate(year, month, 1)
        val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
        val daysInMonth = nextMonth.minus(DatePeriod(days = 1)).day
        var workDays = 0
        var offDays = 0
        var noneDays = 0
        for (d in 1..daysInMonth) {
            when (shiftKindAt(LocalDate(year, month, d))) {
                ShiftKind.WORK -> workDays++
                ShiftKind.OFF -> offDays++
                null -> noneDays++
            }
        }
        return TimePieChartData(
            segments = buildList {
                if (workDays > 0) add(PieSegment("工作", workDays.toFloat(), Color(0xFF5C6BC0)))
                if (offDays > 0) add(PieSegment("休息", offDays.toFloat(), Color(0xFFEF5350)))
                if (noneDays > 0) add(PieSegment("未排班", noneDays.toFloat(), Color(0xFFBDBDBD)))
            },
            title = "${month}月时间分布"
        )
    }
}

private val PieChart_shape = RoundedCornerShape(16.dp)

/**
 * 时间饼图卡片，展示时间分布的甜甜圈图。
 *
 * @param data 饼图数据
 * @param onSegmentClick 分段点击回调（用于下钻）
 * @param modifier 外部布局修饰符
 */
@Composable
fun TimePieChartCard(
    data: TimePieChartData,
    onSegmentClick: ((PieSegment) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedIndex = remember { mutableStateOf(-1) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PieChart_shape,
        color = colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (data.title.isNotEmpty()) {
                Text(text = data.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }

            TimePieChart(
                data = data,
                selectedIndex = selectedIndex.value,
                onSegmentTap = { index ->
                    selectedIndex.value = if (index == selectedIndex.value) -1 else index
                    val segment = data.segments.getOrNull(index)
                    if (segment != null) onSegmentClick?.invoke(segment)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                data.segments.forEachIndexed { index, segment ->
                    if (index > 0) Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = segment.color
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        val pct = if (data.total > 0) (segment.value / data.total * 100).toInt() else 0
                        Text(
                            text = "${segment.label} $pct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index == selectedIndex.value) colorScheme.onSurface else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 甜甜圈饼图，支持分段选择动画。
 *
 * @param data 饼图数据
 * @param selectedIndex 当前选中分段索引，-1 表示无选中
 * @param onSegmentTap 分段点击回调
 * @param modifier 外部布局修饰符
 */
@Composable
fun TimePieChart(
    data: TimePieChartData,
    selectedIndex: Int = -1,
    onSegmentTap: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatable.snapTo(0f)
        animatable.animateTo(1f, animationSpec = tween(600))
    }
    val textMeasurer = rememberTextMeasurer()
    val total = data.total
    if (total <= 0f) return

    val sweepAngles = remember(data, animatable.value) {
        data.segments.map { (it.value / total) * 360f * animatable.value }
    }
    val gapAngle = 1.5f

    Canvas(modifier = modifier.pointerInput(data) {
        detectTapGestures { offset: Offset ->
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f
            val radius = min(w, h) / 2f * 0.85f
            val dx = offset.x - centerX
            val dy = offset.y - centerY
            val dist = sqrt(dx * dx + dy * dy)
            val innerRadius = radius * 0.55f
            if (dist in innerRadius..radius) {
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angle < -90f) angle += 360f
                angle = (angle + 90f + 360f) % 360f
                var cumulative = 0f
                for (i in sweepAngles.indices) {
                    cumulative += sweepAngles[i]
                    if (angle <= cumulative) {
                        onSegmentTap?.invoke(i)
                        break
                    }
                }
            }
        }
    }) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f * 0.85f
        val innerRadius = radius * 0.55f
        val pullOut = 6.dp.toPx()

        var startAngle = -90f
        data.segments.forEachIndexed { index, segment ->
            val sweep = sweepAngles[index]
            if (sweep <= 0f) {
                startAngle += sweep
                return@forEachIndexed
            }
            val midAngle = startAngle + sweep / 2f
            val isSelected = index == selectedIndex
            val offsetX = if (isSelected) cos(Math.toRadians(midAngle.toDouble())).toFloat() * pullOut else 0f
            val offsetY = if (isSelected) sin(Math.toRadians(midAngle.toDouble())).toFloat() * pullOut else 0f

            drawArc(
                color = segment.color,
                startAngle = startAngle + gapAngle / 2,
                sweepAngle = (sweep - gapAngle).coerceAtLeast(0f),
                useCenter = false,
                topLeft = Offset(center.x - radius + offsetX, center.y - radius + offsetY),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = radius - innerRadius)
            )
            startAngle += sweep
        }

        val selected = data.segments.getOrNull(selectedIndex)
        if (selected != null) {
            val pct = (selected.value / total * 100).toInt()
            val pctLayout = textMeasurer.measure(
                "${pct}%",
                TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = selected.color)
            )
            drawText(
                textLayoutResult = pctLayout,
                topLeft = Offset(center.x - pctLayout.size.width / 2f, center.y - pctLayout.size.height)
            )
            val labelLayout = textMeasurer.measure(
                selected.label,
                TextStyle(fontSize = 13.sp, color = Color.Gray)
            )
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(center.x - labelLayout.size.width / 2f, center.y + 4.dp.toPx())
            )
        }
    }
}
