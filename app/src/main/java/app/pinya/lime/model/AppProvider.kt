package app.pinya.lime.model

class AppProvider {
    companion object {
        fun refreshAppList(): MutableList<AppModel> {
            println("REFRESH")
            return mutableListOf<AppModel>()
        }
    }
}