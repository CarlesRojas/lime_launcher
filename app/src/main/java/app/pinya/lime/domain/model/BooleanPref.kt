package app.pinya.lime.domain.model

enum class BooleanPref {
    GENERAL_SHOW_HIDDEN_APPS,
    GENERAL_SHOW_STATUS_BAR,
    GENERAL_CHANGE_WALLPAPER_DAILY,
    GENERAL_ALSO_CHANGE_LOCK_SCREEN,
    GENERAL_IS_TEXT_BLACK,
    GENERAL_DIM_BACKGROUND,

    HOME_SHOW_IN_GRID,
    HOME_SHOW_LABELS,
    HOME_SHOW_ICONS,

    DRAWER_SHOW_IN_GRID,
    DRAWER_SHOW_LABELS,
    DRAWER_SHOW_ICONS,
    DRAWER_SHOW_SEARCH_BAR,
    DRAWER_AUTO_SHOW_KEYBOARD,
    DRAWER_AUTO_OPEN_APPS,
    DRAWER_SHOW_ALPHABET_FILTER,

    TIME_VISIBLE,

    DATE_VISIBLE,
}