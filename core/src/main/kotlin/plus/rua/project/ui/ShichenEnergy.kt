package plus.rua.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * 十二时辰信息。
 */
@Immutable
data class ShichenInfo(
    val name: String,
    val startHour: Int,
    val endHour: Int,
    val element: String,
    val elementColor: Color,
    val description: String
)

private val SHICHEN_LIST = listOf(
    ShichenInfo("子时", 23, 1, "水", Color(0xFF5C6BC0), "夜半安眠，养精蓄锐"),
    ShichenInfo("丑时", 1, 3, "土", Color(0xFF8D6E63), "鸡鸣时分，肝经当令"),
    ShichenInfo("寅时", 3, 5, "木", Color(0xFF66BB6A), "平旦将明，肺经最旺"),
    ShichenInfo("卯时", 5, 7, "木", Color(0xFF66BB6A), "日出东方，大肠经旺"),
    ShichenInfo("辰时", 7, 9, "土", Color(0xFF8D6E63), "食时养胃，辰时进餐"),
    ShichenInfo("巳时", 9, 11, "火", Color(0xFFEF5350), "隅中暖阳，脾经当令"),
    ShichenInfo("午时", 11, 13, "火", Color(0xFFEF5350), "日正当中，心经最旺"),
    ShichenInfo("未时", 13, 15, "土", Color(0xFF8D6E63), "日昳午后，小肠经旺"),
    ShichenInfo("申时", 15, 17, "金", Color(0xFFFFB74D), "晡时夕照，膀胱经旺"),
    ShichenInfo("酉时", 17, 19, "金", Color(0xFFFFB74D), "日入归家，肾经当令"),
    ShichenInfo("戌时", 19, 21, "土", Color(0xFF8D6E63), "黄昏静思，心包经旺"),
    ShichenInfo("亥时", 21, 23, "水", Color(0xFF5C6BC0), "人定入眠，三焦经通")
)

@OptIn(ExperimentalTime::class)
private fun currentShichenIndex(): Int {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        23, 0 -> 0
        1, 2 -> 1
        3, 4 -> 2
        5, 6 -> 3
        7, 8 -> 4
        9, 10 -> 5
        11, 12 -> 6
        13, 14 -> 7
        15, 16 -> 8
        17, 18 -> 9
        19, 20 -> 10
        21, 22 -> 11
        else -> 0
    }
}

private val ShichenCard_shape = RoundedCornerShape(16.dp)

/**
 * 时辰能量卡片，展示十二时辰及其对应五行和当前时辰高亮。
 *
 * @param modifier 外部布局修饰符
 */
@Composable
fun ShichenEnergyCard(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentIndex = remember { currentShichenIndex() }
    val currentShichen = SHICHEN_LIST[currentIndex]

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShichenCard_shape,
        color = colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "时辰能量", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = currentShichen.elementColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "当前: ${currentShichen.name}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = currentShichen.elementColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentShichen.description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(SHICHEN_LIST) { index, shichen ->
                    val isCurrent = index == currentIndex
                    val isPast = index < currentIndex
                    ShichenItem(
                        shichen = shichen,
                        isCurrent = isCurrent,
                        isPast = isPast,
                        modifier = Modifier.width(58.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShichenItem(
    shichen: ShichenInfo,
    isCurrent: Boolean,
    isPast: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = when {
        isCurrent -> shichen.elementColor.copy(alpha = 0.15f)
        isPast -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = shichen.name,
                fontSize = 12.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) shichen.elementColor else colorScheme.onSurface
            )
            Text(
                text = "${shichen.startHour}:${if (shichen.startHour < 10) "00" else "00"}",
                fontSize = 9.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = if (isPast) 0.4f else 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) shichen.elementColor else shichen.elementColor.copy(alpha = 0.3f))
            )
            Text(
                text = shichen.element,
                fontSize = 9.sp,
                color = shichen.elementColor.copy(alpha = if (isPast) 0.4f else 0.8f)
            )
        }
    }
}
