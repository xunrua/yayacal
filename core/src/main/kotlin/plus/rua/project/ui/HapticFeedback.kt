package plus.rua.project.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * 触觉反馈控制器，提供不同力度的震动反馈。
 */
class HapticFeedbackController(private val view: View) {

    /** 轻微触感，适合翻页滑动时的刻度反馈。 */
    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** 中等触感，适合页面切换完成时的确认反馈。 */
    fun click() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    /** 强烈触感，适合重要操作（如长按切换视图）的反馈。 */
    fun heavyClick() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * 创建并记住触觉反馈控制器。
 *
 * @return HapticFeedbackController 实例
 */
@Composable
fun rememberHapticFeedback(): HapticFeedbackController {
    val view = LocalView.current
    return remember(view) { HapticFeedbackController(view) }
}
