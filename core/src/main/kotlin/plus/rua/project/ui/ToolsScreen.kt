package plus.rua.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * 工具页面，提供实用工具功能入口。
 *
 * @param onBack 返回回调
 * @param modifier 布局修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("工具") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        val arrowColor = MaterialTheme.colorScheme.onSurface
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = arrowColor,
                                start = Offset(size.width * 0.75f, size.height * 0.15f),
                                end = Offset(size.width * 0.25f, size.height * 0.5f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = arrowColor,
                                start = Offset(size.width * 0.25f, size.height * 0.5f),
                                end = Offset(size.width * 0.75f, size.height * 0.85f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "敬请期待",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
