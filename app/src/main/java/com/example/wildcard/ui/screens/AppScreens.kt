package com.example.wildcard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun UserRegistrationScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ユーザー名登録画面")
        Button(onClick = { navController.navigate("room") }) {
            Text("登録して次へ")
        }
    }
}

@Composable
fun RoomScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ルーム作成/参加画面")
        Button(onClick = { navController.navigate("dashboard") }) {
            Text("入室")
        }
    }
}

@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "待機/ダッシュボード画面")
        Button(onClick = { navController.navigate("mission") }) {
            Text("ミッション開始")
        }
        Button(onClick = { navController.navigate("remote_control") }) {
            Text("お仕置き開始")
        }
    }
}

@Composable
fun MissionScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "起床ミッション画面")
        Button(onClick = { navController.navigate("dashboard") }) {
            Text("クリア")
        }
        Button(onClick = { navController.popBackStack() }) {
            Text("失敗 (戻る)")
        }
    }
}

@Composable
fun RemoteControlScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "遠隔操作画面")
        Button(onClick = { navController.navigate("dashboard") }) {
            Text("終了")
        }
    }
}
