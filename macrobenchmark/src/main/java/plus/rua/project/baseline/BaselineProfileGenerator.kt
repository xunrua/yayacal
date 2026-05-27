package plus.rua.project.baseline

import android.util.Log
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
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
                val TAG = "BaselineProfile"

                // 1. 冷启动：从 launcher 启动应用
                pressHome()
                device.executeShellCommand(
                    "am start -W -n plus.rua.project/.MainActivity"
                )
                device.waitForIdle()

                // 2. 展开 FAB 菜单，等待菜单项出现
                val fab = device.findObject(By.res("plus.rua.project:id/fab_menu"))
                assertNotNull("FAB 按钮必须存在", fab)
                fab!!.click()
                val yearViewItem = device.wait(Until.findObject(By.text("年视图")), 3000)
                Log.d(TAG, "FAB 菜单展开: yearViewItem=${yearViewItem != null}")

                // 3. 切换到年视图（覆盖 YearGridView、YearHeader、MiniMonth 路径）
                assertNotNull("年视图菜单项必须出现", yearViewItem)
                yearViewItem!!.click()
                val yearGrid = device.wait(Until.findObject(By.res("plus.rua.project:id/year_grid")), 3000)
                Log.d(TAG, "年视图加载: yearGrid=${yearGrid != null}")
                assertNotNull("YearGridView 必须加载", yearGrid)
                device.waitForIdle()

                // 4. 在年视图中滑动到不同年份（覆盖动画和分页路径）
                yearGrid!!.swipe(Direction.UP, 0.5f)
                device.waitForIdle()
                yearGrid.swipe(Direction.DOWN, 0.5f)
                device.waitForIdle()

                // 5. 展开 FAB 并切换回月视图
                val fabForMonth = device.findObject(By.res("plus.rua.project:id/fab_menu"))
                assertNotNull("FAB 按钮必须存在（返回月视图）", fabForMonth)
                fabForMonth!!.click()
                val monthViewItem = device.wait(Until.findObject(By.text("月视图")), 3000)
                Log.d(TAG, "FAB 菜单展开: monthViewItem=${monthViewItem != null}")
                assertNotNull("月视图菜单项必须出现", monthViewItem)
                monthViewItem!!.click()
                device.waitForIdle()

                // 6. 点击某一天（覆盖 DayCell 点击路径 + 底部卡片展开）
                val todayCell = device.findObject(By.descContains("今天"))
                    ?: device.findObject(By.text("21"))
                assertNotNull("DayCell 必须可点击", todayCell)
                todayCell!!.click()
                device.waitForIdle()

                // 7. 拖拽 BottomCard 触发月视图↔周视图折叠/展开
                val bottomCard = device.findObject(By.res("plus.rua.project:id/bottom_card"))
                assertNotNull("BottomCard 必须存在", bottomCard)
                val bounds = bottomCard!!.visibleBounds
                val centerX = bounds.centerX()
                val centerY = bounds.centerY()
                val dragDistance = (bounds.height() * 0.4).toInt()
                // 向上拖拽 → 折叠到周视图
                device.drag(centerX, centerY, centerX, centerY - dragDistance, 20)
                device.waitForIdle()
                // 向下拖拽 → 展开到月视图
                device.drag(centerX, centerY - dragDistance, centerX, centerY, 20)
                device.waitForIdle()

                // 8. 展开 FAB 并进入工具页面
                val fabForTools = device.findObject(By.res("plus.rua.project:id/fab_menu"))
                assertNotNull("FAB 按钮必须存在（工具页）", fabForTools)
                fabForTools!!.click()
                val toolsButton = device.wait(Until.findObject(By.text("工具")), 3000)
                Log.d(TAG, "FAB 菜单展开: toolsButton=${toolsButton != null}")
                assertNotNull("工具菜单项必须出现", toolsButton)
                toolsButton!!.click()
                device.waitForIdle()

                // 9. 进入日期检查器（覆盖 DateCheckerScreen）
                val dateCheckerEntry = device.wait(
                    Until.findObject(By.res("plus.rua.project:id/tool_date_checker")), 3000
                )
                assertNotNull("日期检查器入口必须存在", dateCheckerEntry)
                dateCheckerEntry!!.click()
                device.waitForIdle()

                // 10. 点击日历图标打开 DatePickerDialog（覆盖 DatePicker）
                val datePickerBtn = device.wait(
                    Until.findObject(By.res("plus.rua.project:id/date_picker_button")), 3000
                )
                if (datePickerBtn != null) {
                    datePickerBtn.click()
                    device.waitForIdle()
                }

                // 11. 等待 DatePickerDialog 并点击确定
                device.wait(Until.findObject(By.text("确定")), 2000)
                val confirmBtn = device.findObject(By.text("确定"))
                if (confirmBtn != null) {
                    confirmBtn.click()
                    device.waitForIdle()
                }

                // 12. 点击 FAB 添加新行（覆盖 FAB + LazyColumn items 重组）
                val dateCheckerFab = device.findObject(By.res("plus.rua.project:id/date_checker_fab"))
                if (dateCheckerFab != null) {
                    dateCheckerFab.click()
                    device.waitForIdle()
                }

                // 13. 返回工具页
                device.pressBack()
                device.waitForIdle()

                // 14. 返回主界面
                device.pressBack()
                device.waitForIdle()

                // 15. 左右滑动切换月份（覆盖 CalendarPager 翻页）
                val calendarPager = device.findObject(By.res("plus.rua.project:id/calendar_pager"))
                assertNotNull("CalendarPager 必须存在", calendarPager)
                calendarPager!!.swipe(Direction.LEFT, 0.5f)
                device.waitForIdle()
                calendarPager.swipe(Direction.RIGHT, 0.5f)
                device.waitForIdle()

                // 16. 进入关于页面（覆盖 AboutScreen + AnimatedGif）
                val fabForAbout = device.findObject(By.res("plus.rua.project:id/fab_menu"))
                assertNotNull("FAB 按钮必须存在（关于页）", fabForAbout)
                fabForAbout!!.click()
                val aboutButton = device.wait(Until.findObject(By.text("关于")), 3000)
                assertNotNull("关于菜单项必须出现", aboutButton)
                aboutButton!!.click()
                device.waitForIdle()

                // 17. 进入开源许可页面（覆盖 LicensesScreen）
                val licensesButton = device.wait(Until.findObject(By.text("开源许可")), 3000)
                assertNotNull("开源许可按钮必须存在", licensesButton)
                licensesButton!!.click()
                device.waitForIdle()

                // 18. 等待许可列表加载
                device.wait(Until.findObject(By.textContains("Apache")), 2000)

                // 19. 返回关于页
                device.pressBack()
                device.waitForIdle()

                // 20. 返回主界面
                device.pressBack()
                device.waitForIdle()

                Log.d(TAG, "Baseline profile 生成完成，所有路径已覆盖")
            }
        )
    }
}
