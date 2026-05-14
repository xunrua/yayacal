package plus.rua.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import plus.rua.project.ui.CalendarMonthView

/**
 * 应用入口 Composable，包裹 CalendarMonthView 并提供 MaterialTheme。
 */
@Composable
@Preview(name = "Calendar App")
fun App() {
    MaterialTheme {
        CalendarMonthView(modifier = Modifier)
    }
}
