package com.example.handhelp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.handhelp.ui.screens.*
import com.example.handhelp.ui.screens.auth.*
import com.example.handhelp.ui.screens.role.RoleSelectionScreen
import com.example.handhelp.ui.screens.volunteer.*
import com.example.handhelp.ui.screens.organizer.*
import com.example.handhelp.viewmodel.AuthViewModel
import com.example.handhelp.viewmodel.NotificationViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.WELCOME) {
            WelcomeScreen(navController = navController)
        }
        composable(NavRoutes.LOGIN) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.ROLE_SELECTION) {
            RoleSelectionScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.VOLUNTEER_HOME) {
            VolunteerHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(
            route = NavRoutes.MISSION_DETAIL,
            arguments = listOf(navArgument("missionId") { type = NavType.StringType })
        ) { backStackEntry ->
            MissionDetailScreen(
                navController = navController,
                missionId = backStackEntry.arguments?.getString("missionId") ?: "",
                authViewModel = authViewModel
            )
        }
        composable(NavRoutes.SEARCH) {
            SearchScreen(navController = navController)
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationsScreen(navController = navController,
                authViewModel = authViewModel)
        }
        composable(NavRoutes.HISTORY) {
            HistoryScreen(navController = navController,
            authViewModel = authViewModel)
        }
        composable(NavRoutes.ORGANIZER_HOME) {
            OrganizerHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(NavRoutes.ADD_MISSION) {
            AddMissionScreen(navController = navController,
                authViewModel = authViewModel)
        }
    }
}