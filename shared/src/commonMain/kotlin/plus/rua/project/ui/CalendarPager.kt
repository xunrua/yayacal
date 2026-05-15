package plus.rua.project.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * 月度日历分页器，HorizontalPager 实现无限左右滑动切换月份。
 *
 * @param selectedDate 当前选中日期
 * @param today 今天的日期
 * @param onDateClick 日期点击回调
 * @param onMonthChanged 月份切换回调，滑动到新月份时触发
 * @param collapseProgress 折叠进度，0f=展开，1f=折叠
 * @param rowHeightPx 从外层传入的锁定行高（像素），折叠过程中不变
 * @param modifier 外部布局修饰符
 */
@Composable
fun CalendarPager(
    selectedDate: LocalDate,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    onMonthChanged: (year: Int, month: Int) -> Unit,
    collapseProgress: Float,
    rowHeightPx: Int,
    effectiveWeeks: Float,
    onRowHeightMeasured: ((Int) -> Unit)? = null,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val initialYear = remember { today.year }
    val initialMonth = remember { today.month.number }
    val coroutineScope = rememberCoroutineScope()

    // Sync settled page to onMonthChanged (skip initial emission to preserve "today" selection)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.drop(1).collect { page ->
            val yearMonth = pageToYearMonth(page, initialYear, initialMonth)
            onMonthChanged(yearMonth.first, yearMonth.second)
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        modifier = modifier
    ) { page ->
        val (year, month) = pageToYearMonth(page, initialYear, initialMonth)
        CalendarMonthPage(
            year = year,
            month = month,
            selectedDate = selectedDate,
            today = today,
            onDateClick = { date ->
                onDateClick(date)
                // If clicking a date in a different month, scroll to that page
                val clickedYear = date.year
                val clickedMonth = date.month.number
                if (clickedYear != year || clickedMonth != month) {
                    val targetPage = yearMonthToPage(clickedYear, clickedMonth, initialYear, initialMonth)
                    if (targetPage != pagerState.currentPage) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                }
            },
            collapseProgress = collapseProgress,
            rowHeightPx = rowHeightPx,
            effectiveWeeks = effectiveWeeks,
            onRowHeightMeasured = onRowHeightMeasured
        )
    }
}