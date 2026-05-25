package plus.rua.project

import android.os.Trace
import plus.rua.project.shared.BuildConfig

/**
 * Systrace 包装，用于录制 Compose 性能 trace。
 * 仅在 debug 构建中启用，release 构建中为空操作。
 */
fun composeTraceBeginSection(name: String) {
    if (!BuildConfig.DEBUG) return
    try {
        Trace.beginSection(name)
    } catch (_: RuntimeException) {
        // Trace API 在 host test 中未 stub；忽略
    }
}

fun composeTraceEndSection() {
    if (!BuildConfig.DEBUG) return
    try {
        Trace.endSection()
    } catch (_: RuntimeException) {
        // Trace API 在 host test 中未 stub；忽略
    }
}
