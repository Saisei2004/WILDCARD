package com.example.wildcard.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.wildcard.ui.dashboard.DashboardScreen
import com.example.wildcard.ui.dashboard.DashboardViewModel
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
    // アニメーション対応のNavControllerを使用します
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current

    // スライドアニメーションの定義
    val slideInRight = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500))
    val slideOutLeft = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500))
    val slideInLeft = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500))
    val slideOutRight = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500))

    // NavHostをAnimatedNavHostに置き換えます
    AnimatedNavHost(navController = navController, startDestination = AppScreen.Registration.route) {

        // ユーザー登録/ルーム参加画面
        composable(
            route = AppScreen.Registration.route,
            exitTransition = { slideOutLeft },      // この画面から去るとき（左へスライドアウト）
            popEnterTransition = { slideInLeft }   // 他の画面から戻ってくるとき（左からスライドイン）
        ) {
            UserRegistrationScreen(navController = navController)
        }

        // ダッシュボード画面
        composable(
            route = AppScreen.Dashboard.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
            enterTransition = { slideInRight },     // この画面に入るとき（右からスライドイン）
            exitTransition = { slideOutLeft },      // この画面から去るとき
            popEnterTransition = { slideInLeft },   // 他の画面から戻ってくるとき
            popExitTransition = { slideOutRight }   // この画面から「戻る」操作をしたとき
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(roomId, context.applicationContext)
            )
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // ミッション画面
        composable(
            route = AppScreen.Mission.route,
            enterTransition = { slideInRight },
            exitTransition = { slideOutLeft },
            popEnterTransition = { slideInLeft },
            popExitTransition = { slideOutRight }
        ) {
            MissionScreen(navController = navController)
        }

        // 遠隔操作画面
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

// ViewModelに引数を渡すためのFactoryクラス (変更なし)
class DashboardViewModelFactory(
    private val roomId: String,
    private val applicationContext: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(roomId, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
