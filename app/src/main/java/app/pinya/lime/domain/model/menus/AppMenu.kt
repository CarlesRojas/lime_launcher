package app.pinya.lime.domain.model.menus

import androidx.constraintlayout.widget.ConstraintLayout
import app.pinya.lime.domain.model.AppModel

data class AppMenu(
    val app: AppModel,
    val fromHome: Boolean,
    val container: ConstraintLayout,
)