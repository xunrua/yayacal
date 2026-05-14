package plus.rua.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import plus.rua.project.ui.CalendarMonthView

@Composable
@Preview
fun App() {
    MaterialTheme {
        CalendarMonthView(modifier = Modifier)
    }
}
