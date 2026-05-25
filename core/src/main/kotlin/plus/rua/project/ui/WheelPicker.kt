package plus.rua.project.ui

import android.os.Build
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val ItemHeight = 48.dp
private const val VisibleItemCount = 5
private val WheelHeight = ItemHeight * VisibleItemCount

/**
 * 通用滚轮选择器，支持惯性吸附和触觉反馈。
 *
 * @param items 显示的项目列表
 * @param selectedIndex 当前选中项索引
 * @param onSelectedChange 选中项变化回调
 * @param modifier 外部布局修饰符
 * @param itemContent 单个项目渲染，[isSelected] 为 true 表示中心选中项
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int, item: String, isSelected: Boolean) -> Unit = { _, item, isSelected ->
        Text(
            text = item,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = if (isSelected) 20.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = LocalTextStyle.current
        )
    }
) {
    val paddingItems = VisibleItemCount / 2
    val totalItems = items.size + paddingItems * 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - paddingItems).coerceAtLeast(0)
    )
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    fun centerForLayoutIndex(layoutIndex: Int): Int = layoutIndex - paddingItems

    fun layoutIndexForCenter(center: Int): Int = center + paddingItems

    // 检测中心选中项变化 → 触觉反馈
    val currentCenter by remember {
        derivedStateOf {
            val viewportCenter = listState.layoutInfo.viewportSize.height / 2f
            listState.layoutInfo.visibleItemsInfo.minByOrNull {
                abs(it.offset + it.size / 2f - viewportCenter)
            }?.index?.let { centerForLayoutIndex(it) } ?: -1
        }
    }

    LaunchedEffect(currentCenter) {
        if (currentCenter in items.indices && currentCenter != selectedIndex) {
            onSelectedChange(currentCenter)
            performHapticFeedback(view)
        }
    }

    // 初始滚动到选中项
    LaunchedEffect(selectedIndex) {
        val target = layoutIndexForCenter(selectedIndex)
        if (centerForLayoutIndex(listState.firstVisibleItemIndex) != selectedIndex) {
            listState.scrollToItem((target - paddingItems).coerceAtLeast(0))
        }
    }

    // 滚动停止后吸附到最近项
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    val target = layoutIndexForCenter(currentCenter.coerceIn(0, items.lastIndex))
                    val current = listState.firstVisibleItemIndex + paddingItems
                    if (target != current) {
                        coroutineScope.launch {
                            listState.animateScrollToItem((target - paddingItems).coerceAtLeast(0))
                        }
                    }
                }
            }
    }

    val snapLayoutInfoProvider = remember(listState) {
        SnapLayoutInfoProvider(listState)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(WheelHeight),
        flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = true
    ) {
        items(totalItems) { layoutIndex ->
            val centerIndex = centerForLayoutIndex(layoutIndex)
            val isValid = centerIndex in items.indices
            Box(
                modifier = Modifier
                    .height(ItemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isValid) {
                    itemContent(centerIndex, items[centerIndex], centerIndex == currentCenter)
                }
            }
        }
    }
}

private fun performHapticFeedback(view: android.view.View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
    } else {
        @Suppress("DEPRECATION")
        view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
    }
}
