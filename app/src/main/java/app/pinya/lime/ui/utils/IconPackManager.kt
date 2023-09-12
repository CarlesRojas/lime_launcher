package app.pinya.lime.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.Xml
import androidx.core.graphics.drawable.toDrawable
import app.pinya.lime.domain.model.Icon
import app.pinya.lime.domain.model.IconRule
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@Suppress("SpellCheckingInspection", "DiscouragedApi", "Unused")
open class IconPackManager(mContext: Context) {
    private val contextRes = mContext.resources
    private val pm: PackageManager = mContext.packageManager
    private val flag = PackageManager.GET_META_DATA
    private val customRules = hashMapOf<String, String>()
    private val themes = mutableListOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")
    private var iconPacks: HashMap<String?, IconPack>? = null

    open fun isSupportedIconPacks() = isSupportedIconPacks(false)

    open fun isSupportedIconPacks(reload: Boolean): HashMap<String?, IconPack> {
        try {
            if (iconPacks == null || reload) {
                iconPacks = hashMapOf()
                themes.forEach {
                    val intent = Intent(it)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.queryIntentActivities(
                            intent, PackageManager.ResolveInfoFlags.of(flag.toLong())
                        )
                    } else {
                        pm.queryIntentActivities(intent, flag)
                    }.forEach { info ->
                        val iconPackPackageName = info.activityInfo.packageName
                        try {
                            val appInfo = getApplicationInfo(iconPackPackageName)

                            if (appInfo != null) {
                                iconPacks!![iconPackPackageName] = IconPack(
                                    iconPackPackageName,
                                    pm.getApplicationLabel(appInfo).toString()
                                )
                            }
                        } catch (_: PackageManager.NameNotFoundException) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }

        return iconPacks ?: hashMapOf()
    }

    private fun getApplicationInfo(iconPackPackageName: String): ApplicationInfo? {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(
                    iconPackPackageName,
                    PackageManager.ApplicationInfoFlags.of(flag.toLong())
                )
            } else {
                pm.getApplicationInfo(iconPackPackageName, flag)
            }
        } catch (_: Exception) {
        }

        return null
    }

    inner class IconPack(private val packageName: String, val name: String) {
        private val drawables = hashMapOf<String?, String?>()
        private val iconPackRes = pm.getResourcesForApplication(packageName)

        fun getPackageName(): String = packageName

        init {
            try {
                iconPackRes.assets.open("appfilter.xml").use {
                    Xml.newPullParser().run {
                        setInput(it.reader())
                        var eventType = eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && name == "item") {
                                val componentValue = getAttributeValue(null, "component")
                                if (!drawables.containsKey(componentValue)) drawables[componentValue] =
                                    getAttributeValue(null, "drawable")
                            }
                            eventType = next()
                        }
                    }
                }
            } catch (_: XmlPullParserException) {
                Log.d(TAG, "Cannot parse icon pack appfilter.xml")
            } catch (_: Exception) {
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun getDrawable(appPackageName: String): Drawable? {
            var drawableValue =
                drawables[pm.getLaunchIntentForPackage(appPackageName)?.component.toString()]

            if (!drawableValue.isNullOrEmpty()) {
                val id = iconPackRes.getIdentifier(drawableValue, "drawable", packageName)
                if (id > 0) return iconPackRes.getDrawable(id, null) //load icon from pack
            }
            return null
        }

        private fun keywordMatches(keyword: String, key: String, value: String): Boolean {
            return if (keyword.length > 1) key.contains(keyword) || value.contains(keyword) else value.startsWith(keyword)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun getIconsForKeyword(keyword: String): Set<Icon> {
            val icons = mutableSetOf<Icon>()
            val iconIds = mutableSetOf<Int>()
            val maxIcons = 256

            drawables.filter {
                keywordMatches(keyword, it.key ?: "", it.value ?: "")
            }.forEach {
                val id = iconPackRes.getIdentifier(it.value, "drawable", packageName)
                val key = it.key

                if (key != null && id > 0 && !iconIds.contains(id) && icons.size < maxIcons) {
                    try {
                        icons.add(Icon(key, iconPackRes.getDrawable(id, null)))
                        iconIds.add(id)
                    } catch (_: Exception) {
                    }
                }
            }

            return icons
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun loadIconFromRule(rule: IconRule): Drawable? {
            var drawableValue: String? = null

            drawables.filter {
                it.key?.contains(rule.icon) == true
            }.firstNotNullOfOrNull {
                drawableValue = it.value
            }

            if (!drawableValue.isNullOrEmpty()) {
                val id = iconPackRes.getIdentifier(drawableValue, "drawable", packageName)
                if (id > 0) return iconPackRes.getDrawable(id, null) //load icon from pack
            }
            return null
        }

        fun loadIcon(info: ApplicationInfo) = getDrawable(info.packageName)

        fun iconCutCircle(icon: Bitmap) = iconCutCircle(icon, 0.9f)

        private fun iconCutCircle(icon: Bitmap, scale: Float): BitmapDrawable {
            val side = icon.width / 2f
            val bmp = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
            val paint = Paint()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            val canvas = Canvas(bmp)
            canvas.scale(scale, scale, canvas.width / 2.toFloat(), canvas.height / 2.toFloat())
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(side, side, side, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val rect = Rect(0, 0, bmp.width, bmp.height)
            canvas.drawBitmap(icon, rect, rect, paint)
            return bmp.toDrawable(contextRes)
        }
    }

    companion object {
        private const val TAG = "IconPackManager"
    }
}