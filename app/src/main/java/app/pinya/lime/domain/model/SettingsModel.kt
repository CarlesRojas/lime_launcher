package app.pinya.lime.domain.model

data class SettingsModel(
    // GENERAL
    var generalShowHiddenApps: Boolean = false,
    var generalIsTextBlack: Boolean = false,
    var generalDimBackground: Boolean = true,
    var generalChangeWallpaperDaily: Boolean = false,
    var generalShowStatusBar: Boolean = false,

    // DATE
    var dateFormat: Int = 1,
    var dateVisible: Boolean = true,
    var dateClickApp: String = "default",

    // TIME
    var timeFormat: Int = 0,
    var timeVisible: Boolean = true,
    var timeClickApp: String = "default",

    // HOME
    var homeShowIcons: Boolean = true,
    var homeShowInGrid: Boolean = false,
    var homeShowLabels: Boolean = true,

    // DRAWER
    var drawerShowIcons: Boolean = true,
    var drawerShowInGrid: Boolean = false,
    var drawerShowLabels: Boolean = true,
    var drawerAutoShowKeyboard: Boolean = true,
    var drawerAutoOpenApps: Boolean = true,
    var drawerShowSearchBar: Boolean = true,
    var drawerShowAlphabetFilter: Boolean = true,
)