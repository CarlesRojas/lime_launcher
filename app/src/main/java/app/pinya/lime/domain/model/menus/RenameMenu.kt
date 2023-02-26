package app.pinya.lime.domain.model.menus

import androidx.constraintlayout.widget.ConstraintLayout
import app.pinya.lime.domain.model.AppModel

data class RenameMenu(
    val app: AppModel,
    val container: ConstraintLayout,
)