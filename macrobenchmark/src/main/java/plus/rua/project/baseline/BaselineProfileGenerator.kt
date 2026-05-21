package plus.rua.project.baseline

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile 自动生成器。
 *
 * 运行方式（一键生成 + 自动复制到 :core）：
 * ```
 * ./gradlew :macrobenchmark:updateBaselineProfile
 * ```
 *
 * 仅运行基准测试（不自动复制）：
 * ```
 * ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
 * ```
 *
 * 手动复制路径：
 * `macrobenchmark/build/outputs/connected_android_test_additional_output/`
 *
 * 测试覆盖全部用户交互路径，实现全量 AOT：
 * 1. 冷启动 → 首帧渲染
 * 2. FAB 展开 → 年视图 → 月视图
 * 3. 日期选择 → 周视图折叠/展开
 * 4. 关于页 → 开源许可页
 * 5. 返回主界面
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateAppStartupProfile() {
        baselineProfileRule.collect(
            packageName = "plus.rua.project",
            includeInStartupProfile = true,
            profileBlock = {
                // 1. 冷启动：从 launcher 启动应用
                // 注：使用 shell command 绕过 startActivityAndWait，因为模拟器的 software
                // renderer 不支持 gfxinfo framestats，会导致 amStartAndWait 超时。
                pressHome()
                device.executeShellCommand(
                    "am start -W -n plus.rua.project/.MainActivity"
                )
                device.waitForIdle()

                // 3. 模拟用户交互：展开 FAB 菜单
                val fab = device.findObject(By.res("plus.rua.project:id/fab_menu"))
                if (fab != null) {
                    fab.click()
                    device.waitForIdle()
                }

                // 4. 切换到年视图（覆盖 YearGridView、YearHeader、MiniMonth 路径）
                val yearViewButton = device.findObject(By.text("年视图"))
                if (yearViewButton != null) {
                    yearViewButton.click()
                    device.waitForIdle()
                }

                // 5. 在年视图中滑动到不同年份（覆盖动画和分页路径）
                val yearGrid = device.findObject(By.res("plus.rua.project:id/year_grid"))
                if (yearGrid != null) {
                    yearGrid.swipe(Direction.UP, 0.5f)
                    device.waitForIdle()
                    yearGrid.swipe(Direction.DOWN, 0.5f)
                    device.waitForIdle()
                }

                // 6. 切换回月视图
                val monthViewButton = device.findObject(By.text("月视图"))
                if (monthViewButton != null) {
                    monthViewButton.click()
                    device.waitForIdle()
                }

                // 7. 点击某一天（覆盖 DayCell 点击路径 + 底部卡片展开）
                val todayCell = device.findObject(By.descContains("今天"))
                    ?: device.findObject(By.text("21"))
                if (todayCell != null) {
                    todayCell.click()
                    device.waitForIdle()
                }

                // 8. 上下滑动触发月视图↔周视图切换（覆盖 BottomCard 拖拽 + collapse 动画）
                val calendarArea = device.findObject(By.res("plus.rua.project:id/calendar_pager"))
                    ?: device.findObject(By.textContains("2026"))
                if (calendarArea != null) {
                    calendarArea.swipe(Direction.UP, 0.5f)
                    device.waitForIdle()
                    calendarArea.swipe(Direction.DOWN, 0.5f)
                    device.waitForIdle()
                }

                // 9. 左右滑动切换月份（覆盖 CalendarPager 翻页）
                if (calendarArea != null) {
                    calendarArea.swipe(Direction.LEFT, 0.5f)
                    device.waitForIdle()
                    calendarArea.swipe(Direction.RIGHT, 0.5f)
                    device.waitForIdle()
                }

                // 10. 进入关于页面（覆盖 AboutScreen + AnimatedGif）
                val aboutButton = device.findObject(By.text("关于"))
                if (aboutButton != null) {
                    aboutButton.click()
                    device.waitForIdle()
                }

                // 11. 进入开源许可页面（覆盖 LicensesScreen）
                val licensesButton = device.findObject(By.text("开源许可"))
                if (licensesButton != null) {
                    licensesButton.click()
                    device.waitForIdle()
                }

                // 12. 等待许可列表加载
                device.wait(Until.findObject(By.textContains("Apache")), 2000)

                // 13. 返回关于页
                device.pressBack()
                device.waitForIdle()

                // 14. 返回主界面
                device.pressBack()
                device.waitForIdle()
            }
        )
    }
}
