package com.alf06.document.reader.utils

class PreferenceHelper(preference: SharedPreference) {

    var isRatedApp = preference.observable("isRatedApp", false)
    var languageSelected by preference.string("language_code", "en")
    var isFinishFirstFlow by preference.boolean("is_finish_first_flow", false)

    var countSessionApp by preference.int("count_session_app", 0)
    var countUserSession by preference.int("countUserSession", 0)
    var isShowTutorial by preference.boolean("is_show_tutorial", false)
    fun isUfo(): Boolean = countSessionApp == 0

}