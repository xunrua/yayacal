package plus.rua.project

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tyme.solar.SolarDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * 动态快捷方式管理器，根据日历状态更新应用快捷方式。
 */
class DynamicShortcutManager(private val context: Context) {

    private val shortcutManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.getSystemService(ShortcutManager::class.java)
        } else null
    }

    /**
     * 根据当前日历状态更新动态快捷方式。
     */
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun updateShortcuts(
        today: LocalDate,
        shiftPattern: ShiftPattern?,
        selectedDate: LocalDate
    ) {
        val manager = shortcutManager ?: return
        val maxShortcuts = manager.maxShortcutCountPerActivity.coerceAtMost(5)

        val shortcuts = buildList {
            add(shortcutGoToday(today))

            if (shiftPattern != null) {
                add(shortcutShiftOverview(shiftPattern, today))
                add(shortcutNextDayOff(shiftPattern, today))
            }

            add(shortcutSolarTerm(today))
            add(shortcutMoonPhase(today))
        }.take(maxShortcuts)

        try {
            manager.dynamicShortcuts = shortcuts
        } catch (_: Exception) {
            // 部分设备 ShortcutManager 不可用
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun shortcutGoToday(today: LocalDate): ShortcutInfo {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { putExtra("action", "go_today") }
            ?: Intent(Intent.ACTION_MAIN)

        return ShortcutInfo.Builder(context, "go_today")
            .setShortLabel("回到今天")
            .setLongLabel("回到今天 ${today.year}年${today.monthNumber}月${today.day}日")
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun shortcutShiftOverview(pattern: ShiftPattern, today: LocalDate): ShortcutInfo {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { putExtra("action", "shift_overview") }
            ?: Intent(Intent.ACTION_MAIN)

        return ShortcutInfo.Builder(context, "shift_overview")
            .setShortLabel("排班概览")
            .setLongLabel("查看${pattern.name}排班周期")
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun shortcutNextDayOff(pattern: ShiftPattern, today: LocalDate): ShortcutInfo {
        var date = today.plus(kotlinx.datetime.DatePeriod(days = 1))
        for (i in 0..60) {
            if (pattern.kindAt(date) == ShiftKind.OFF) break
            date = date.plus(kotlinx.datetime.DatePeriod(days = 1))
        }
        val daysUntil = today.daysUntil(date)
        val label = when {
            daysUntil == 1 -> "明天休息"
            daysUntil <= 7 -> "${daysUntil}天后休息"
            else -> "下个休息日"
        }

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { putExtra("action", "next_day_off") }
            ?: Intent(Intent.ACTION_MAIN)

        return ShortcutInfo.Builder(context, "next_day_off")
            .setShortLabel(label)
            .setLongLabel("下一个休息日: ${date.monthNumber}月${date.day}日")
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    @Suppress("DEPRECATION")
    private fun shortcutSolarTerm(today: LocalDate): ShortcutInfo {
        val solarDay = SolarDay.fromYmd(today.year, today.monthNumber, today.day)
        val currentTerm = solarDay.getTermDay()
        val termName = if (currentTerm.getDayIndex() == 0) currentTerm.getSolarTerm().getName() else null

        val label = termName?.let { "今日$it" } ?: "节气查询"
        val longLabel = termName?.let { "今天是$it" } ?: "查看二十四节气"

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { putExtra("action", "solar_term") }
            ?: Intent(Intent.ACTION_MAIN)

        return ShortcutInfo.Builder(context, "solar_term")
            .setShortLabel(label)
            .setLongLabel(longLabel)
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun shortcutMoonPhase(today: LocalDate): ShortcutInfo {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { putExtra("action", "moon_phase") }
            ?: Intent(Intent.ACTION_MAIN)

        return ShortcutInfo.Builder(context, "moon_phase")
            .setShortLabel("月相")
            .setLongLabel("查看今日月相")
            .setIntent(intent)
            .build()
    }
}
