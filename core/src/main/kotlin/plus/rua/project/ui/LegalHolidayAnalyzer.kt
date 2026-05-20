package plus.rua.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyme.solar.SolarDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import plus.rua.project.ShiftKind

/**
 * 假勤博弈统计结果。
 */
@Immutable
data class HolidayBattleResult(
    val workDaysOffHoliday: Int,
    val offDaysOnHoliday: Int,
    val totalWorkDays: Int,
    val totalOffDays: Int,
    val totalLegalHolidays: Int,
    val score: Int,
    val rating: String
)

/**
 * 分析周期类型。
 */
enum class AnalysisPeriod(val label: String) {
    MONTH("本月"),
    QUARTER("本季"),
    YEAR("本年")
}

/**
 * 假勤博弈分析器，对比个人轮班与法定假日。
 */
object LegalHolidayAnalyzer {

    @Suppress("DEPRECATION")
    fun analyze(
        startDate: LocalDate,
        endDate: LocalDate,
        shiftKindAt: (LocalDate) -> ShiftKind?
    ): HolidayBattleResult {
        var workDaysOffHoliday = 0
        var offDaysOnHoliday = 0
        var totalWorkDays = 0
        var totalOffDays = 0
        var totalLegalHolidays = 0

        val total = startDate.daysUntil(endDate) + 1
        for (i in 0 until total) {
            val date = startDate.plus(kotlinx.datetime.DatePeriod(days = i))
            val shift = shiftKindAt(date) ?: continue
            val solarDay = SolarDay.fromYmd(date.year, date.monthNumber, date.day)
            val legalHoliday = solarDay.getLegalHoliday()

            if (shift == ShiftKind.WORK) totalWorkDays++ else totalOffDays++

            if (legalHoliday != null) {
                totalLegalHolidays++
                if (legalHoliday.isWork()) {
                    // 法定调休上班日
                    if (shift == ShiftKind.OFF) workDaysOffHoliday++
                } else {
                    // 法定假日
                    if (shift == ShiftKind.WORK) offDaysOnHoliday++
                }
            }
        }

        val winScore = workDaysOffHoliday * 2
        val loseScore = offDaysOnHoliday * 2
        val score = winScore - loseScore
        val rating = when {
            score >= 10 -> "大赢家"
            score >= 4 -> "小赢家"
            score >= -3 -> "持平"
            score >= -8 -> "小亏"
            else -> "血亏"
        }

        return HolidayBattleResult(
            workDaysOffHoliday = workDaysOffHoliday,
            offDaysOnHoliday = offDaysOnHoliday,
            totalWorkDays = totalWorkDays,
            totalOffDays = totalOffDays,
            totalLegalHolidays = totalLegalHolidays,
            score = score,
            rating = rating
        )
    }

    fun dateRangeForPeriod(period: AnalysisPeriod, today: LocalDate): Pair<LocalDate, LocalDate> = when (period) {
        AnalysisPeriod.MONTH -> {
            @Suppress("DEPRECATION")
            val start = LocalDate(today.year, today.monthNumber, 1)
            val nextMonth = if (today.monthNumber == 12) LocalDate(today.year + 1, 1, 1)
            else LocalDate(today.year, today.monthNumber + 1, 1)
            start to nextMonth.minus(kotlinx.datetime.DatePeriod(days = 1))
        }
        AnalysisPeriod.QUARTER -> {
            val quarterStartMonth = ((today.monthNumber - 1) / 3) * 3 + 1
            val start = LocalDate(today.year, quarterStartMonth, 1)
            val endMonth = quarterStartMonth + 2
            val end = if (endMonth > 12) {
                LocalDate(today.year + 1, endMonth - 12, 31)
            } else {
                val nextMonthAfterEnd = if (endMonth == 12) LocalDate(today.year + 1, 1, 1)
                else LocalDate(today.year, endMonth + 1, 1)
                nextMonthAfterEnd.minus(kotlinx.datetime.DatePeriod(days = 1))
            }
            start to end
        }
        AnalysisPeriod.YEAR -> {
            val start = LocalDate(today.year, 1, 1)
            val end = LocalDate(today.year, 12, 31)
            start to end
        }
    }
}

private val BattleCard_shape = RoundedCornerShape(16.dp)

/**
 * 假勤博弈卡片，显示排班与法定假日对比统计。
 *
 * @param result 分析结果
 * @param period 当前分析周期
 * @param onPeriodChange 周期切换回调
 * @param modifier 外部布局修饰符
 */
@Composable
fun HolidayBattleCard(
    result: HolidayBattleResult,
    period: AnalysisPeriod,
    onPeriodChange: (AnalysisPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val scoreColor = when {
        result.score > 0 -> colorScheme.primary
        result.score < 0 -> colorScheme.error
        else -> colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BattleCard_shape,
        color = colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "假勤博弈", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AnalysisPeriod.entries.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (p == period) colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.clickable { onPeriodChange(p) }
                        ) {
                            Text(
                                text = p.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = if (p == period) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "赚了", value = "${result.workDaysOffHoliday}天", color = colorScheme.primary)
                StatItem(label = "亏了", value = "${result.offDaysOnHoliday}天", color = colorScheme.error)
                StatItem(label = "法定假", value = "${result.totalLegalHolidays}天", color = colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.rating,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (result.score >= 0) "+${result.score}" else "${result.score}",
                    fontSize = 14.sp,
                    color = scoreColor
                )
            }

            if (result.totalWorkDays > 0 || result.totalOffDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                BattleBarChart(result = result)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BattleBarChart(result: HolidayBattleResult, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val total = (result.totalWorkDays + result.totalOffDays).coerceAtLeast(1)
    val workRatio = result.totalWorkDays.toFloat() / total
    val offRatio = result.totalOffDays.toFloat() / total

    Canvas(modifier = modifier.fillMaxWidth().height(8.dp)) {
        drawRoundRect(
            color = colorScheme.primary.copy(alpha = 0.3f),
            topLeft = Offset.Zero,
            size = Size(size.width * workRatio, size.height),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawRoundRect(
            color = colorScheme.error.copy(alpha = 0.3f),
            topLeft = Offset(size.width * workRatio, 0f),
            size = Size(size.width * offRatio, size.height),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}
