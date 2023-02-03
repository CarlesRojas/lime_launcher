package app.pinya.lime.model

import android.graphics.drawable.Drawable

data class AppModel(
    var originalName: String,
    var packageName: String,
    var icon: Drawable,

    var name: String,
    var home: Boolean = false,
    var hidden: Boolean = false,
    var system: Boolean = false,
    var homeOrderIndex: Int = 0
)