package app.pinya.lime.domain.model.menus

import androidx.constraintlayout.widget.ConstraintLayout
import app.pinya.lime.domain.model.AppModel

data class ChangeAppIconMenu(
    val app: AppModel,
    val container: ConstraintLayout,
)