package com.example.handhelp.navigation

object NavRoutes {
    // Auth
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ROLE_SELECTION = "role_selection"

    // Bénévole
    const val VOLUNTEER_HOME = "volunteer_home"
    const val MISSION_DETAIL = "mission_detail/{missionId}"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"
    const val HISTORY = "history"

    // Organisateur
    const val ORGANIZER_HOME = "organizer_home"
    const val ADD_MISSION = "add_mission"

    // Helpers
    fun missionDetail(missionId: String) = "mission_detail/$missionId"
}