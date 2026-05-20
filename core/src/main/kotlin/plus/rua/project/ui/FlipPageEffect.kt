package plus.rua.project.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * 3D 翻页效果 Modifier，根据页面偏移施加 rotationY 变换。
 *
 * 滑动时页面绕 Y 轴微旋转，产生翻书般的立体感。
 * 旋转幅度由 [maxRotation] 控制，默认 15°，足以感知深度但不遮挡内容。
 *
 * @param pageOffset 当前页偏移量 (-1 ~ 1)
 * @param maxRotation 最大旋转角度（度）
 */
@Composable
fun Modifier.flipPageEffect(
    pageOffset: Float,
    maxRotation: Float = 15f
): Modifier = this.graphicsLayer {
    val rotation = pageOffset * maxRotation
    this.rotationY = rotation
    cameraDistance = 8f * density
    val absOffset = pageOffset.absoluteValue
    val scale = 1f - absOffset * 0.05f
    scaleX = scale
    scaleY = scale
    alpha = 1f - absOffset * 0.15f
}

/**
 * 记住指定页面在 PagerState 中的偏移量。
 *
 * 返回值范围约 -1 ~ 1，0 表示页面恰好居中，
 * 正值表示页面在左侧（向右滑），负值表示在右侧（向左滑）。
 *
 * @param pagerState 分页器状态
 * @param page 目标页码
 */
@Composable
fun rememberPageOffsetFraction(
    pagerState: PagerState,
    page: Int
): Float {
    return remember(pagerState, page) {
        derivedStateOf {
            val currentPage = pagerState.currentPage
            val currentOffset = pagerState.currentPageOffsetFraction
            val diff = page - currentPage
            if (diff == 0) currentOffset
            else if (diff > 0) 1f - currentOffset
            else -1f + currentOffset
        }
    }.value
}
