package com.example.wildcard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wildcard.ui.registration.UserRegistrationScreen
import com.example.wildcard.ui.dashboard.DashboardScreen
import com.example.wildcard.ui.mission.MissionScreen
import com.example.wildcard.ui.remotecontrol.RemoteControlScreen

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
        composable("dashboard_route") {
            DashboardScreen(navController = navController)
        }
        // ミッション画面
        composable("mission_route") {
            MissionScreen(navController = navController)
        }
        // 遠隔操作画面
        composable("remote_control_route") {
            RemoteControlScreen(navController = navController)
        }
    }
}
