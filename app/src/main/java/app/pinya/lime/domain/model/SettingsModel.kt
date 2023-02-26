package app.pinya.lime.domain.model

data class SettingsModel(
    // GENERAL
    var generalShowHiddenApps: Boolean = false,
    var generalShowStatusBar: Boolean = false,
    var generalChangeWallpaperDaily: Boolean = false,
    var generalIsTextBlack: Boolean = false,
    var generalDimBackground: Boolean = true,

    // HOME
    var homeShowInGrid: Boolean = false,
    var homeShowLabels: Boolean = true,
    var homeShowIcons: Boolean = true,

    // DRAWER
    var drawerShowInGrid: Boolean = false,
    var drawerShowLabels: Boolean = true,
    var drawerShowIcons: Boolean = true,
    var drawerShowSearchBar: Boolean = true,
    var drawerAutoShowKeyboard: Boolean = true,
    var drawerAutoOpenApps: Boolean = true,
    var drawerShowAlphabetFilter: Boolean = true,

    // DATE
    var dateVisible: Boolean = true,
    var dateFormat: Int = 1,
    var dateClickApp: String = "default",

    // TIME
    var timeVisible: Boolean = true,
    var timeFormat: Int = 0,
    var timeClickApp: String = "default",

    )