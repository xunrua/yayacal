package plus.rua.project.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import plus.rua.project.CalendarViewModel

/**
 * 日历主界面，包含月/周视图切换和折叠动画。
 *
 * 折叠时日历从月视图（6行）收缩为周视图（1行），BottomCard 同步上移填充空间。
 *
 * @param modifier 外部布局修饰符
 */
@Composable
fun CalendarMonthView(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { CalendarViewModel(coroutineScope) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var currentYear by remember { mutableIntStateOf(viewModel.currentYear) }
    var currentMonth by remember { mutableIntStateOf(viewModel.currentMonth) }
    val density = LocalDensity.current

    var calendarHeightPx by remember { mutableIntStateOf(0) }
    var screenHeightPx by remember { mutableIntStateOf(0) }
    var expandedCalendarHeightPx by remember { mutableIntStateOf(0) }
    var monthHeaderHeightPx by remember { mutableIntStateOf(0) }
    var weekdayHeaderHeightPx by remember { mutableIntStateOf(0) }

    // 日历网格高度 = 总高度 - MonthHeader - WeekdayHeader
    val expandedGridHeightPx = expandedCalendarHeightPx - monthHeaderHeightPx - weekdayHeaderHeightPx
    // 折叠偏移量 = 进度 × 网格5行高度（保留1行可见）
    val collapseOffsetPx = if (viewModel.isCollapsed) {
        0
    } else {
        -(viewModel.collapseProgress * expandedGridHeightPx * 5f / 6f).toInt()
    }
    val cardTopPx = if (viewModel.isCollapsed) {
        calendarHeightPx
    } else {
        expandedCalendarHeightPx + collapseOffsetPx
    }
    val cardHeightPx = screenHeightPx - cardTopPx

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .onSizeChanged { size ->
                screenHeightPx = size.height
            }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).onSizeChanged { size ->
            calendarHeightPx = size.height
            // 仅在首次展开时记录完整日历高度，折叠后不再覆盖
            if (!viewModel.isCollapsed && viewModel.collapseProgress < 0.01f) {
                expandedCalendarHeightPx = size.height
            }
        }) {
            MonthHeader(
                year = currentYear,
                month = currentMonth,
                weekNumber = viewModel.getIsoWeekNumber(viewModel.selectedDate),
                modifier = Modifier.onSizeChanged { size ->
                    monthHeaderHeightPx = size.height
                }
            )
            WeekdayHeader(
                modifier = Modifier.fillMaxWidth().onSizeChanged { size ->
                    weekdayHeaderHeightPx = size.height
                }
            )
            if (viewModel.isCollapsed) {
                WeekPager(
                    selectedDate = viewModel.selectedDate,
                    today = today,
                    onDateClick = { date -> viewModel.selectDate(date) },
                    onWeekChanged = { weekMonday ->
                        currentYear = weekMonday.year
                        @Suppress("DEPRECATION") // monthNumber 无替代 API，kotlinx-datetime 尚未提供新接口
                        currentMonth = weekMonday.monthNumber
                    }
                )
            } else {
                CalendarPager(
                    selectedDate = viewModel.selectedDate,
                    today = today,
                    onDateClick = { date -> viewModel.selectDate(date) },
                    onMonthChanged = { year, month ->
                        currentYear = year
                        currentMonth = month
                    },
                    collapseProgress = viewModel.collapseProgress
                )
            }
        }
        if (cardHeightPx > 0) {
            BottomCard(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { cardHeightPx.toDp() })
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
