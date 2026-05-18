package plus.rua.project

import android.os.Trace

actual fun composeTraceBeginSection(name: String) = Trace.beginSection(name)

actual fun composeTraceEndSection() = Trace.endSection()