package app.pinya.lime.domain.model.menus

import androidx.constraintlayout.widget.ConstraintLayout

data class AppListMenu(
    val isTime: Boolean,
    val container: ConstraintLayout,
    val onChangeAppCallback: (useDefault: Boolean, useNone: Boolean, newAppPackage: String) -> Unit
)
