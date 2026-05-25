package plus.rua.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import plus.rua.project.ui.AboutScreen

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AboutScreen(
                onBack = { finishWithSlideBack() },
                onNavigateToLicenses = {
                    startActivityWithSlide(Intent(this, LicensesActivity::class.java))
                }
            )
        }
    }
}
