package com.example.wildcard.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.wildcard.ui.dashboard.DashboardScreen
import com.example.wildcard.ui.mission.MissionScreen
import com.example.wildcard.ui.remotecontrol.RemoteControlScreen
import com.example.wildcard.ui.registration.UserRegistrationScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

/**
 * 各画面のルート（識別名）を管理します。
 */
sealed class AppScreen(val route: String) {
    object Registration : AppScreen("registration_route")
    object Dashboard : AppScreen("dashboard_route/{roomId}") {
        fun createRoute(roomId: String) = "dashboard_route/$roomId"
    }
    object Mission : AppScreen("mission_route")
    object RemoteControl : AppScreen("remote_control_route/{targetUserId}") {
        fun createRoute(targetUserId: String) = "remote_control_route/$targetUserId"
    }
}

/**
 * アプリケーションのナビゲーションを定義します。
 * スライドアニメーションによる画面遷移を実装します。
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    val slideInRight = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500))
    val slideOutLeft = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500))
    val slideInLeft = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500))
    val slideOutRight = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500))

    AnimatedNavHost(navController = navController, startDestination = AppScreen.Registration.route) {

        composable(
            route = AppScreen.Registration.route,
            exitTransition = { slideOutLeft },
            popEnterTransition = { slideInLeft }
        ) {
            UserRegistrationScreen(navController = navController)
        }

        composable(
            route = AppScreen.Dashboard.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
            enterTransition = { slideInRight },
            exitTransition = { slideOutLeft },
            popEnterTransition = { slideInLeft },
            popExitTransition = { slideOutRight }
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            DashboardScreen(
                navController = navController,
                roomId = roomId,
                applicationContext = LocalContext.current
            )
        }

        composable(
            route = AppScreen.Mission.route,
            enterTransition = { slideInRight },
            exitTransition = { slideOutLeft },
            popEnterTransition = { slideInLeft },
            popExitTransition = { slideOutRight }
        ) {
            MissionScreen(navController = navController)
        }

        composable(
            route = AppScreen.RemoteControl.route,
            arguments = listOf(navArgument("targetUserId") { type = NavType.StringType }),
            enterTransition = { slideInRight },
            popExitTransition = { slideOutRight }
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
            RemoteControlScreen(navController = navController, targetUserId = targetUserId)
        }
    }
}
