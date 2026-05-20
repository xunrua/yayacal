package plus.rua.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyme.solar.SolarDay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** 明信片模板风格。 */
enum class PostcardTemplate {
    MINIMAL,
    TRADITIONAL,
    GRADIENT
}

@Immutable
internal data class PostcardSeasonColors(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color
)

private val seasonGradients = mapOf(
    1 to PostcardSeasonColors(Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF1565C0)),
    2 to PostcardSeasonColors(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF2E7D32)),
    3 to PostcardSeasonColors(Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFE65100)),
    4 to PostcardSeasonColors(Color(0xFFFBE9E7), Color(0xFFFFCCBC), Color(0xFFBF360C)),
    12 to PostcardSeasonColors(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF37474F))
)

private fun seasonForMonth(month: Int): Int = when (month) {
    in 3..5 -> 2
    in 6..8 -> 3
    in 9..11 -> 4
    else -> 1
}

private fun weekdayLabel(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "星期一"
    DayOfWeek.TUESDAY -> "星期二"
    DayOfWeek.WEDNESDAY -> "星期三"
    DayOfWeek.THURSDAY -> "星期四"
    DayOfWeek.FRIDAY -> "星期五"
    DayOfWeek.SATURDAY -> "星期六"
    DayOfWeek.SUNDAY -> "星期日"
}

@Suppress("DEPRECATION")
private fun lunarDateString(date: LocalDate): String {
    val solarDay = SolarDay.fromYmd(date.year, date.monthNumber, date.day)
    val lunarDay = solarDay.getLunarDay()
    val lunarMonth = lunarDay.getLunarMonth()
    return "${lunarMonth.getName()}${lunarDay.getName()}"
}

@Suppress("DEPRECATION")
private fun solarTermForDate(date: LocalDate): String? {
    val solarDay = SolarDay.fromYmd(date.year, date.monthNumber, date.day)
    val termDay = solarDay.getTermDay()
    return if (termDay.getDayIndex() == 0) termDay.getSolarTerm().getName() else null
}

@Suppress("DEPRECATION")
private fun holidayForDate(date: LocalDate): String? {
    val solarDay = SolarDay.fromYmd(date.year, date.monthNumber, date.day)
    val festival = solarDay.getFestival()
    if (festival != null) return festival.getName()
    val lunarFestival = solarDay.getLunarDay().getFestival()
    if (lunarFestival != null) return lunarFestival.getName()
    return null
}

/**
 * 日历明信片，将选中日期渲染为可分享的卡片视图。
 *
 * @param date 选中的日期
 * @param template 明信片模板风格
 * @param modifier 外部布局修饰符
 */
@Composable
fun CalendarPostcard(
    date: LocalDate,
    template: PostcardTemplate = PostcardTemplate.MINIMAL,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val lunarDate = remember(date) { lunarDateString(date) }
    val weekday = remember(date) { weekdayLabel(date.dayOfWeek) }
    val solarTerm = remember(date) { solarTermForDate(date) }
    val holiday = remember(date) { holidayForDate(date) }
    val season = seasonForMonth(date.monthNumber)
    val colors = seasonGradients[season] ?: seasonGradients[1]!!

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        when (template) {
            PostcardTemplate.MINIMAL -> MinimalPostcard(
                date = date, lunarDate = lunarDate, weekday = weekday,
                solarTerm = solarTerm, holiday = holiday
            )
            PostcardTemplate.TRADITIONAL -> TraditionalPostcard(
                date = date, lunarDate = lunarDate, weekday = weekday,
                solarTerm = solarTerm, holiday = holiday, colors = colors
            )
            PostcardTemplate.GRADIENT -> GradientPostcard(
                date = date, lunarDate = lunarDate, weekday = weekday,
                solarTerm = solarTerm, holiday = holiday, colors = colors
            )
        }
    }
}

@Composable
private fun MinimalPostcard(
    date: LocalDate,
    lunarDate: String,
    weekday: String,
    solarTerm: String?,
    holiday: String?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${date.year}年${date.monthNumber}月",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Text(text = weekday, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "农历 $lunarDate", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)
        if (solarTerm != null) {
            Text(text = solarTerm, color = colorScheme.error, fontSize = 13.sp)
        }
        if (holiday != null) {
            Text(text = holiday, color = colorScheme.primary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TraditionalPostcard(
    date: LocalDate,
    lunarDate: String,
    weekday: String,
    solarTerm: String?,
    holiday: String?,
    colors: PostcardSeasonColors,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = colors.accent.copy(alpha = 0.3f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFDF6E3))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 3.dp.toPx()
            val inset = 12.dp.toPx()
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
            val cornerLen = 16.dp.toPx()
            val innerInset = inset + strokeWidth / 2
            val accentColor = colors.accent
            val corners = listOf(
                Offset(innerInset, innerInset) to Offset(innerInset + cornerLen, innerInset),
                Offset(innerInset, innerInset) to Offset(innerInset, innerInset + cornerLen),
                Offset(size.width - innerInset, innerInset) to Offset(size.width - innerInset - cornerLen, innerInset),
                Offset(size.width - innerInset, innerInset) to Offset(size.width - innerInset, innerInset + cornerLen),
                Offset(innerInset, size.height - innerInset) to Offset(innerInset + cornerLen, size.height - innerInset),
                Offset(innerInset, size.height - innerInset) to Offset(innerInset, size.height - innerInset - cornerLen),
                Offset(size.width - innerInset, size.height - innerInset) to Offset(size.width - innerInset - cornerLen, size.height - innerInset),
                Offset(size.width - innerInset, size.height - innerInset) to Offset(size.width - innerInset, size.height - innerInset - cornerLen)
            )
            corners.forEach { (start, end) ->
                drawLine(accentColor, start, end, strokeWidth = 2.dp.toPx())
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = lunarDate, style = MaterialTheme.typography.bodySmall, color = colors.accent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )
            Text(
                text = "${date.year}年${date.monthNumber}月",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF795548)
            )
            Text(text = weekday, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8D6E63))
            if (solarTerm != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = solarTerm, color = colors.accent, fontWeight = FontWeight.Medium)
            }
            if (holiday != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = holiday, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun GradientPostcard(
    date: LocalDate,
    lunarDate: String,
    weekday: String,
    solarTerm: String?,
    holiday: String?,
    colors: PostcardSeasonColors,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.gradientStart, colors.gradientEnd)
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension * 0.4f
            drawCircle(
                color = colors.accent.copy(alpha = 0.08f),
                radius = radius,
                center = Offset(size.width * 0.8f, size.height * 0.3f)
            )
            drawCircle(
                color = colors.accent.copy(alpha = 0.05f),
                radius = radius * 0.6f,
                center = Offset(size.width * 0.2f, size.height * 0.7f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = lunarDate, style = MaterialTheme.typography.labelMedium, color = colors.accent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${date.year}年${date.monthNumber}月",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            if (solarTerm != null || holiday != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = listOfNotNull(solarTerm, holiday).joinToString(" · "),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
