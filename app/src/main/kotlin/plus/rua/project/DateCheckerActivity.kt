package plus.rua.project

import android.os.Bundle
import androidx.activity.compose.setContent
import plus.rua.project.ui.DateCheckerScreen

class DateCheckerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DateCheckerScreen(
                onBack = { finishWithSlideBack() }
            )
        }
    }
}
