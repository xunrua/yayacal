package plus.rua.project

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * 个人轮班类型。仅区分上班与休息;后续可扩展早/中/晚班、休假等。
 */
enum class ShiftKind { WORK, OFF }

/**
 * 单日排班覆盖记录。
 *
 * @param date 覆盖日期
 * @param kind 覆盖后的班次
 * @param reason 覆盖原因（如"加班"、"调休"）
 */
data class ShiftOverride(
    val date: LocalDate,
    val kind: ShiftKind,
    val reason: String = ""
)

/**
 * 智能排班建议结果。
 */
data class ShiftSuggestion(
    val suggestedCycle: List<ShiftKind>,
    val suggestedAnchor: LocalDate,
    val confidence: Float,
    val matchRate: Float
)

/**
 * 个人轮班周期。
 *
 * 与法定节假日完全独立:周期内某天是 WORK 还是 OFF,只看
 * `(date - anchorDate) mod cycle.size` 在 cycle 中的取值,不受任何节假日/调休影响。
 *
 * @param anchorDate 周期基准日,对应 cycle[0]
 * @param cycle 一个周期内的班次序列,例如 [WORK, WORK, OFF, OFF] 表示 "2 班 2 休"
 * @param name 方案名,用于后续多套方案场景
 * @param overrides 按日期覆盖默认周期计算的班次
 */
data class ShiftPattern(
    val anchorDate: LocalDate,
    val cycle: List<ShiftKind>,
    val name: String = "默认",
    val overrides: Map<LocalDate, ShiftOverride> = emptyMap()
) {
    fun kindAt(date: LocalDate): ShiftKind? {
        overrides[date]?.let { return it.kind }
        if (cycle.isEmpty()) return null
        val diff = anchorDate.daysUntil(date)
        val size = cycle.size
        val idx = ((diff % size) + size) % size
        return cycle[idx]
    }

    fun withOverride(date: LocalDate, kind: ShiftKind, reason: String = ""): ShiftPattern =
        copy(overrides = overrides + (date to ShiftOverride(date, kind, reason)))

    fun removeOverride(date: LocalDate): ShiftPattern =
        copy(overrides = overrides - date)
}

private fun cycleIndex(anchorDate: LocalDate, date: LocalDate, cycleSize: Int): Int {
    val diff = anchorDate.daysUntil(date)
    return ((diff % cycleSize) + cycleSize) % cycleSize
}

/**
 * 智能排班引擎，通过滑动窗口分析手动覆盖记录来学习排班模式变化。
 */
class SmartShiftEngine(
    private val pattern: ShiftPattern,
    private val windowDays: Int = 90
) {
    /**
     * 分析指定日期范围内的覆盖记录，检测排班模式是否发生变化。
     */
    fun analyzeOverrides(today: LocalDate): ShiftSuggestion? {
        if (pattern.cycle.isEmpty()) return null
        val startDate = today.minus(DatePeriod(days = windowDays))
        val relevantEntries = pattern.overrides.entries
            .filter { (date, _) -> date >= startDate && date <= today }
            .sortedBy { it.key }

        if (relevantEntries.size < 5) return null

        val basePredicted = (0 until windowDays).map { offset ->
            val date = startDate.plus(DatePeriod(days = offset))
            val predicted = pattern.kindAt(date) ?: return null
            val actual = pattern.overrides[date]?.kind ?: predicted
            predicted to actual
        }

        val matches = basePredicted.count { it.first == it.second }
        val matchRate = matches.toFloat() / basePredicted.size

        if (matchRate > 0.85f) return null

        val recentDates = relevantEntries.takeLast(30).map { it.key }
        if (recentDates.size < 10) return null

        val bestCycle = detectCycleFromOverrides(recentDates)
            ?: return null

        return ShiftSuggestion(
            suggestedCycle = bestCycle,
            suggestedAnchor = recentDates.first(),
            confidence = matchRate.coerceIn(0f, 1f),
            matchRate = matchRate
        )
    }

    private fun detectCycleFromOverrides(dates: List<LocalDate>): List<ShiftKind>? {
        if (pattern.cycle.isEmpty()) return null
        val cycleSize = pattern.cycle.size

        val samples = dates.mapNotNull { date ->
            val kind = pattern.overrides[date]?.kind ?: return@mapNotNull null
            val idx = cycleIndex(pattern.anchorDate, date, cycleSize)
            idx to kind
        }

        val grouped = samples.groupBy { it.first }
        return (0 until cycleSize).map { idx ->
            val kinds = grouped[idx]?.map { pair -> pair.second } ?: return null
            val workCount = kinds.count { it == ShiftKind.WORK }
            val offCount = kinds.count { it == ShiftKind.OFF }
            if (workCount >= offCount) ShiftKind.WORK else ShiftKind.OFF
        }
    }
}
