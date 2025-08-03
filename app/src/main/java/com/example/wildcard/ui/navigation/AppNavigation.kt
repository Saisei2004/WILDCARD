package com.example.wildcard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wildcard.ui.dashboard.DashboardScreen
import com.example.wildcard.ui.dashboard.DashboardViewModel
import com.example.wildcard.ui.mission.MissionScreen
import com.example.wildcard.ui.remotecontrol.RemoteControlScreen
import com.example.wildcard.ui.registration.UserRegistrationScreen

/**
 * アプリケーションのナビゲーションを定義します。
 *
 * 各画面へのルートと、それらの間の遷移ロジックを管理します。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "registration_route") {
        // ユーザー登録/ルーム参加画面
        composable("registration_route") {
            UserRegistrationScreen(navController = navController)
        }
        // ダッシュボード画面
        composable(
            route = "dashboard_route/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(roomId)
            )
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
        // ミッション画面
        composable("mission_route") {
            MissionScreen(navController = navController)
        }
        // 遠隔操作画面
        composable(
            // 【変更点】ルートに引数 {targetUserId} を追加
            route = "remote_control_route/{targetUserId}",
            arguments = listOf(navArgument("targetUserId") { type = NavType.StringType })
        ) { backStackEntry ->
            // val targetUserId = backStackEntry.arguments?.getString("targetUserId")
            RemoteControlScreen(navController = navController)
        }
    }
}

// ViewModelに引数を渡すためのFactoryクラス
class DashboardViewModelFactory(private val roomId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(roomId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}