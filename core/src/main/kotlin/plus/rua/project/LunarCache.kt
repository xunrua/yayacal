package plus.rua.project

import com.tyme.solar.SolarDay
import kotlinx.datetime.LocalDate

/**
 * 农历/节气/节假日信息缓存。
 *
 * 使用 LinkedHashMap（accessOrder=true）实现 LRU 语义，读写速度优于 ConcurrentHashMap。
 * 通过 @Synchronized 保护并发访问；冷启动时主线程单线程访问，偏向锁使其几乎零开销。
 */
object LunarCache {
    private const val MAX_SIZE = 800

    @Suppress("DEPRECATION")
    private val cache = LinkedHashMap<LocalDate, DayCellInfo>(256, 0.75f, true)

    /**
     * 获取指定日期的信息，缓存 miss 时同步计算。
     */
    @Synchronized
    @Suppress("DEPRECATION") // monthNumber 无替代 API
    fun getOrCompute(date: LocalDate): DayCellInfo {
        cache[date]?.let { return it }
        val computed = compute(date)
        cache[date] = computed
        trimIfNeeded()
        return computed
    }

    /**
     * 批量预计算并填充缓存。
     *
     * @param dates 日期列表
     */
    @Synchronized
    fun precompute(dates: List<LocalDate>) {
        dates.forEach { date ->
            if (!cache.containsKey(date)) {
                cache[date] = compute(date)
            }
        }
        trimIfNeeded()
    }

    private fun trimIfNeeded() {
        if (cache.size > MAX_SIZE) {
            val toRemove = (cache.size * 0.2).toInt().coerceAtLeast(1)
            val iterator = cache.keys.iterator()
            var removed = 0
            while (iterator.hasNext() && removed < toRemove) {
                iterator.next()
                iterator.remove()
                removed++
            }
        }
    }

    @Suppress("DEPRECATION") // monthNumber 无替代 API
    private fun compute(date: LocalDate): DayCellInfo {
        val solarDay = SolarDay.fromYmd(date.year, date.monthNumber, date.day)
        val holidayBadge = solarDay.getLegalHoliday()?.let { if (it.isWork()) "班" else "休" }
        val lunarDay = solarDay.getLunarDay()

        // 农历传统节日（仅当天）
        val lunarFestival = lunarDay.getFestival()
        if (lunarFestival != null) {
            return DayCellInfo(lunarFestival.getName(), true, holidayBadge)
        }

        // 节气（当天才显示）
        val termDay = solarDay.getTermDay()
        if (termDay.getDayIndex() == 0) {
            return DayCellInfo(termDay.getSolarTerm().getName(), true, holidayBadge)
        }

        // 公历节日（仅当天）
        val solarFestival = solarDay.getFestival()
        if (solarFestival != null) {
            return DayCellInfo(solarFestival.getName(), true, holidayBadge)
        }

        // 默认：农历日期
        val name = lunarDay.getName()
        val text = if (name == "初一") {
            val lunarMonth = lunarDay.getLunarMonth()
            "${lunarMonth.getName()}月"
        } else {
            name
        }
        return DayCellInfo(text, false, holidayBadge)
    }
}

/**
 * 日期单元格显示信息。
 *
 * @param annotationText 底部标注文字（农历/节气/节日）
 * @param isAnnotationHighlight 是否为高亮标注（节日/节气）
 * @param holidayBadge 法定调休角标（"班"/"休"/null）
 */
data class DayCellInfo(
    val annotationText: String,
    val isAnnotationHighlight: Boolean,
    val holidayBadge: String?
)
