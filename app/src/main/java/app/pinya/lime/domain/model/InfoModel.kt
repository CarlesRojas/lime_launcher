package app.pinya.lime.domain.model

data class InfoModel(
    var homeApps: MutableSet<String> = mutableSetOf(),
    var hiddenApps: MutableSet<String> = mutableSetOf(),
    var renamedApps: MutableMap<String, String> = mutableMapOf(),
    var iconRules: MutableSet<String> = mutableSetOf(),
    var wallpaperLastUpdatedDate: Int = -1,
    var maxNumberOfHomeApps: Int = 8,
    var tutorialDone: Boolean = false,
)