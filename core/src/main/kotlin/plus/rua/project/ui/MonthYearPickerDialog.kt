package plus.rua.project.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val Years = (1970..2100).map { "${it}年" }
private val Months = (1..12).map { "${it}月" }

/**
 * 年月滚轮选择器弹窗。
 *
 * 左侧年份滚轮 + 右侧月份滚轮，每次滚动触发触觉反馈。
 *
 * @param currentYear 当前年份
 * @param currentMonth 当前月份（1-12）
 * @param onConfirm 确认回调，参数为 (year, month)
 * @param onDismiss 关闭回调
 */
@Composable
fun MonthYearPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    onConfirm: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择年月", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = Years,
                    selectedIndex = selectedYear - 1970,
                    onSelectedChange = { selectedYear = it + 1970 },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                WheelPicker(
                    items = Months,
                    selectedIndex = selectedMonth - 1,
                    onSelectedChange = { selectedMonth = it + 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 年份滚轮选择器弹窗（用于年视图）。
 *
 * @param currentYear 当前年份
 * @param onConfirm 确认回调，参数为 year
 * @param onDismiss 关闭回调
 */
@Composable
fun YearPickerDialog(
    currentYear: Int,
    onConfirm: (year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择年份", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            WheelPicker(
                items = Years,
                selectedIndex = selectedYear - 1970,
                onSelectedChange = { selectedYear = it + 1970 },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
