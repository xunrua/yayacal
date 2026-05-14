package plus.rua.project.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * 月度日历网格页，6×7 布局，支持折叠动画。
 *
 * 折叠时选中行保持原高并向上移动覆盖其他行，其他行保持原位不动。
 * 选中行通过 offset + zIndex 实现覆盖效果。
 *
 * @param year 年份
 * @param month 月份（1-12）
 * @param selectedDate 当前选中日期
 * @param today 今天的日期
 * @param onDateClick 日期点击回调
 * @param collapseProgress 折叠进度，0f=展开（6行），1f=折叠（仅选中行可见）
 * @param modifier 外部布局修饰符
 */
@Composable
fun CalendarMonthPage(
    year: Int,
    month: Int,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    collapseProgress: Float,
    modifier: Modifier = Modifier
) {
    val days = remember(year, month) {
        generateMonthDays(year, month)
    }
    val density = LocalDensity.current

    val weeks = days.chunked(7)
    val selectedWeekIndex = remember(weeks, selectedDate) {
        weeks.indexOfFirst { week -> week.any { it.date == selectedDate } }
    }

    var rowHeightPx by remember { mutableIntStateOf(0) }

    // 选中行上移距离 = 上方行数 × 行高 × progress
    val selectedOffsetPx = if (rowHeightPx > 0) {
        -(selectedWeekIndex.toFloat() * rowHeightPx.toFloat() * collapseProgress)
    } else {
        0f
    }
    val selectedOffsetDp = with(density) { selectedOffsetPx.toDp() }

    Column(modifier = modifier.clipToBounds()) {
        weeks.forEachIndexed { weekIndex, week ->
            val isSelected = weekIndex == selectedWeekIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isSelected) 1f else 0f)
                    .then(
                        if (isSelected && rowHeightPx > 0) {
                            Modifier.offset(y = selectedOffsetDp)
                        } else {
                            Modifier
                        }
                    )
                    .onSizeChanged { size ->
                        if (size.height > 0 && rowHeightPx == 0) {
                            rowHeightPx = size.height
                        }
                    }
                    .padding(vertical = 2.dp)
            ) {
                week.forEach { dayData ->
                    DayCell(
                        date = dayData.date,
                        isCurrentMonth = dayData.isCurrentMonth,
                        isSelected = dayData.date == selectedDate,
                        isToday = dayData.date == today,
                        onClick = { onDateClick(dayData.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class DayData(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Suppress("DEPRECATION")
private fun generateMonthDays(year: Int, month: Int): List<DayData> {
    val firstOfMonth = LocalDate(year, month, 1)
    val offset = firstOfMonth.dayOfWeek.ordinal
    val startDate = firstOfMonth.minus(DatePeriod(days = offset))

    return (0 until 42).map { i ->
        val date = startDate.plus(DatePeriod(days = i))
        DayData(
            date = date,
            isCurrentMonth = date.monthNumber == month && date.year == year
        )
    }
}