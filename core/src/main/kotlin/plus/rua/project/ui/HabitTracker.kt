package plus.rua.project.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.random.Random

/**
 * 习惯数据，记录一个习惯的基本信息和打卡记录。
 */
@Immutable
data class Habit(
    val name: String,
    val targetDaysPerWeek: Int = 7,
    val createdAt: LocalDate,
    val checkIns: Set<LocalDate> = emptySet()
)

/**
 * 连续打卡统计结果。
 */
@Immutable
data class StreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
    val totalCheckIns: Int,
    val totalDays: Int
)

/**
 * 计算习惯的连续打卡统计。
 */
fun calculateStreak(habit: Habit, today: LocalDate): StreakStats {
    val sortedDates = habit.checkIns.sorted()
    if (sortedDates.isEmpty()) {
        val totalDays = habit.createdAt.daysUntil(today).coerceAtLeast(0) + 1
        return StreakStats(0, 0, 0f, 0, totalDays)
    }

    var currentStreak = 0
    var longestStreak = 0
    var streak = 1

    for (i in 1 until sortedDates.size) {
        val diff = sortedDates[i - 1].daysUntil(sortedDates[i])
        if (diff == 1) {
            streak++
        } else {
            longestStreak = max(longestStreak, streak)
            streak = 1
        }
    }
    longestStreak = max(longestStreak, streak)

    val lastCheckIn = sortedDates.last()
    val daysSinceLast = lastCheckIn.daysUntil(today)
    currentStreak = if (daysSinceLast <= 1) streak else 0

    val totalDays = habit.createdAt.daysUntil(today).coerceAtLeast(0) + 1
    val completionRate = if (totalDays > 0) habit.checkIns.size.toFloat() / totalDays else 0f

    return StreakStats(
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        completionRate = completionRate.coerceIn(0f, 1f),
        totalCheckIns = habit.checkIns.size,
        totalDays = totalDays
    )
}

/**
 * 火焰动画组件，根据连续天数调整火焰大小。
 *
 * @param streak 当前连续天数
 * @param modifier 外部布局修饰符
 */
@Composable
fun StreakFlame(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePhase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePhase2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePhase3"
    )

    val colorScheme = MaterialTheme.colorScheme
    val flameScale = (0.6f + (streak.coerceAtMost(30) / 30f) * 0.4f)

    Canvas(modifier = modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f

        val baseColor = colorScheme.error
        val midColor = Color(0xFFFF6D00)
        val topColor = Color(0xFFFFD600)
        val glowColor = baseColor.copy(alpha = 0.15f)

        drawCircle(
            color = glowColor,
            radius = w * 0.45f * flameScale,
            center = Offset(centerX, h * 0.7f),
            alpha = 0.3f + phase1 * 0.2f
        )

        val outerPath = Path().apply {
            moveTo(centerX - w * 0.3f * flameScale, h * 0.85f)
            quadraticBezierTo(
                centerX - w * 0.35f * flameScale, h * (0.4f + phase1 * 0.08f),
                centerX - w * 0.05f * flameScale, h * (0.15f + phase1 * 0.05f)
            )
            quadraticBezierTo(
                centerX, h * (0.05f + phase2 * 0.05f),
                centerX + w * 0.05f * flameScale, h * (0.15f + phase1 * 0.05f)
            )
            quadraticBezierTo(
                centerX + w * 0.35f * flameScale, h * (0.4f + phase2 * 0.08f),
                centerX + w * 0.3f * flameScale, h * 0.85f
            )
            close()
        }
        drawPath(outerPath, baseColor, style = Fill)

        val midPath = Path().apply {
            moveTo(centerX - w * 0.2f * flameScale, h * 0.85f)
            quadraticBezierTo(
                centerX - w * 0.22f * flameScale, h * (0.5f + phase2 * 0.06f),
                centerX, h * (0.25f + phase3 * 0.05f)
            )
            quadraticBezierTo(
                centerX + w * 0.22f * flameScale, h * (0.5f + phase1 * 0.06f),
                centerX + w * 0.2f * flameScale, h * 0.85f
            )
            close()
        }
        drawPath(midPath, midColor, style = Fill)

        val innerPath = Path().apply {
            moveTo(centerX - w * 0.1f * flameScale, h * 0.85f)
            quadraticBezierTo(
                centerX, h * (0.45f + phase3 * 0.04f),
                centerX + w * 0.1f * flameScale, h * 0.85f
            )
            close()
        }
        drawPath(innerPath, topColor, style = Fill)
    }
}

private val HabitTracker_cardShape = RoundedCornerShape(12.dp)

/**
 * 习惯追踪卡片，显示打卡状态和火焰动画。
 *
 * @param habit 习惯数据
 * @param stats 连续打卡统计
 * @param today 今天日期
 * @param modifier 外部布局修饰符
 */
@Composable
fun HabitTrackerCard(
    habit: Habit,
    stats: StreakStats,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HabitTracker_cardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreakFlame(streak = stats.currentStreak)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "连续 ${stats.currentStreak} 天",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Text(
                    text = "最长 ${stats.longestStreak} 天 · ${"%.0f".format(stats.completionRate * 100)}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stats.currentStreak}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                LinearProgressIndicator(
                    progress = { stats.completionRate },
                    modifier = Modifier
                        .width(48.dp)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

/**
 * 打卡热力网格，GitHub contribution 风格展示单习惯打卡记录。
 *
 * @param habit 习惯数据
 * @param today 今天日期
 * @param weeks 显示的周数
 * @param modifier 外部布局修饰符
 */
@Composable
fun HabitCheckInGrid(
    habit: Habit,
    today: LocalDate,
    weeks: Int = 16,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val cellSize = 10.dp
    val gap = 2.dp
    val emptyColor = colorScheme.surfaceVariant
    val fillColor = colorScheme.primary

    Column(modifier = modifier) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(cellSize * 7 + gap * 6)
        ) {
            val cellPx = cellSize.toPx()
            val gapPx = gap.toPx()
            val totalDays = weeks * 7
            val endDate = today
            val startDate = today.minus(DatePeriod(days = totalDays - 1))

            for (week in 0 until weeks) {
                for (day in 0 until 7) {
                    val idx = week * 7 + day
                    val date = startDate.plus(DatePeriod(days = idx))
                    val checked = habit.checkIns.contains(date)
                    val isFuture = date > today

                    val x = week * (cellPx + gapPx)
                    val y = day * (cellPx + gapPx)

                    drawRoundRect(
                        color = when {
                            isFuture -> emptyColor.copy(alpha = 0.3f)
                            checked -> fillColor
                            else -> emptyColor
                        },
                        topLeft = Offset(x, y),
                        size = Size(cellPx, cellPx),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }
        }
    }
}

private val HabitTracker_cardShape2 = RoundedCornerShape(16.dp)

/**
 * 打卡热力网格卡片，包含标题和热力图。
 */
@Composable
fun HabitCheckInGridCard(
    habit: Habit,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HabitTracker_cardShape2,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            HabitCheckInGrid(habit = habit, today = today, weeks = 16)
        }
    }
}
