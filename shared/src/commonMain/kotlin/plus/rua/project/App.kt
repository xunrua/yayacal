package plus.rua.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import plus.rua.project.ui.AboutScreen
import plus.rua.project.ui.CalendarMonthView
import plus.rua.project.ui.LicensesScreen

private enum class Screen { Main, About, Licenses }

/**
 * 应用入口 Composable，根据系统主题切换明暗 ColorScheme 并管理页面导航。
 */
@Composable
@Preview(name = "Calendar App")
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Main) }

    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    // 向前导航：新页面从右侧滑入覆盖，旧页面略微左移+淡出
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                } else {
                    // 向后导航：新页面从左侧滑入，旧页面略微右移+淡出
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 4 } + fadeOut())
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                Screen.Main -> CalendarMonthView(
                    modifier = Modifier,
                    onNavigateToAbout = { currentScreen = Screen.About }
                )
                Screen.About -> {
                    BackHandler { currentScreen = Screen.Main }
                    AboutScreen(
                        onBack = { currentScreen = Screen.Main },
                        onNavigateToLicenses = { currentScreen = Screen.Licenses }
                    )
                }
                Screen.Licenses -> {
                    BackHandler { currentScreen = Screen.About }
                    LicensesScreen(
                        onBack = { currentScreen = Screen.About }
                    )
                }
            }
        }
    }
}
