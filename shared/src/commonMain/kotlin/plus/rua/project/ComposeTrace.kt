package plus.rua.project

/**
 * Systrace 包装，用于录制 Compose 性能 trace。
 * Android 实际调用 android.os.Trace；iOS 为空操作。
 */
expect fun composeTraceBeginSection(name: String)

expect fun composeTraceEndSection()
