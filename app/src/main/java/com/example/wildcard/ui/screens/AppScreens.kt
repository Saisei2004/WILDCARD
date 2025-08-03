package com.example.wildcard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun UserRegistrationScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ユーザー名を入力してください")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("ユーザー名") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("room") }) {
            Text("登録して次へ")
        }
    }
}

@Composable
fun RoomScreen(navController: NavController) {
    var roomName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "合言葉を入力してください")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("合言葉") }
        )
        Spacer(modifier = Modifier.height(16.dp))
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
