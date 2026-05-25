package plus.rua.project

import android.os.Bundle
import androidx.activity.compose.setContent
import plus.rua.project.ui.LicensesScreen

class LicensesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LicensesScreen(
                onBack = { finishWithSlideBack() }
            )
        }
    }
}
