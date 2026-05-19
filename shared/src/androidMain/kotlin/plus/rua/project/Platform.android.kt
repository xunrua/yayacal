package plus.rua.project

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getGifUri(gifFile: String): String = "file:///android_asset/gifs/$gifFile"

actual fun getAppIconUri(): String = "file:///android_asset/app_icon.png"

@Composable
actual fun PredictiveBackHandler(
    enabled: Boolean,
    onProgress: (Float) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    PredictiveBackHandler(enabled = enabled) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { event ->
                onProgress(event.progress)
            }
            onBack()
        } catch (e: CancellationException) {
            onCancel()
        }
    }
}
