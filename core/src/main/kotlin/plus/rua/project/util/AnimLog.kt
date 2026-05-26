package plus.rua.project.util

import android.util.Log
import plus.rua.project.shared.BuildConfig

@Suppress("NOTHING_TO_INLINE")
inline fun logd(tag: String, message: () -> String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message())
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun logd(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}
