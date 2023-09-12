package app.pinya.lime.domain.model

import com.google.gson.Gson

data class IconRule(
    var packageName: String,
    var iconPackContext: String,
    var iconPackName: String,
    var icon: String,
) {
    companion object {
        fun serialize(iconRule: IconRule): String {
            val gson = Gson()
            return gson.toJson(iconRule)
        }

        fun deserialize(json: String): IconRule {
            val gson = Gson()
            return gson.fromJson(json, IconRule::class.java)
        }
    }
}
