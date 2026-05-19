package plus.rua.project

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * 获取 GIF 资源的 URI。
 *
 * @param gifFile GIF 文件名（如 "001.gif"）
 * @return 平台特定的资源 URI
 */
expect fun getGifUri(gifFile: String): String

expect fun getAppIconUri(): String

/**
 * 拦截系统返回手势。
 *
 * @param enabled 是否启用拦截
 * @param onBack 返回回调
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)