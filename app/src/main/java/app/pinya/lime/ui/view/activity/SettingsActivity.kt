package app.pinya.lime.ui.view.activity

import androidx.appcompat.app.AppCompatActivity
import java.text.DateFormat

class SettingsActivity : AppCompatActivity() {

    companion object {
        fun mapFormat(num: Int): Int {
            return when (num) {
                0 -> DateFormat.SHORT
                1 -> DateFormat.MEDIUM
                2 -> DateFormat.LONG
                else -> DateFormat.FULL
            }
        }
    }
}