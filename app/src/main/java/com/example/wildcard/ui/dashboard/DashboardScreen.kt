package com.example.wildcard.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * 待機画面（ダッシュボード）
 *
 * ルームに参加後、ユーザーが待機する画面です。
 * 起床時間の設定、残り時間の表示、参加者リストの表示、
 * およびアラーム時間になった際のミッション開始ボタンの表示を行います。
 * 未起床者がいる場合、そのメンバーをタップすることで遠隔操作画面へ遷移します。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    // TODO: ViewModelなどからルームの状態（起床時間、残り時間、参加者リスト、各メンバーの起床状態）を取得
    val wakeupTime by remember { mutableStateOf("07:00") } // 仮の起床時間
    val remainingTime by remember { mutableStateOf("00:00:00") } // 仮の残り時間
    val participants by remember { mutableStateOf(listOf("Taro (待機中)", "Hanako (起床済み)", "Jiro (ミッション中)")) } // 仮の参加者リスト
    val isAlarmTime by remember { mutableStateOf(false) } // 仮のアラーム時間フラグ
    val missionCleared by remember { mutableStateOf(false) } // 仮のミッションクリアフラグ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "起床時間: $wakeupTime",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "残り時間: $remainingTime",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 参加者リスト
        Text(
            text = "参加者",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        participants.forEach { participant ->
            Text(
                text = participant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { 
                        // TODO: 未起床者の名前をタップした場合、RemoteControlScreenへ遷移
                        // 例: if (participant.contains("未起床")) navController.navigate("remote_control_route")
                    }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // アラーム時間になった際の表示
        if (isAlarmTime) {
            if (!missionCleared) {
                Button(
                    onClick = {
                        // TODO: ミッション開始ロジック
                        navController.navigate("mission_route") // 仮の遷移
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ミッションを開始する")
                }
            } else {
                Text(
                    text = "おはようございます",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    DashboardScreen(rememberNavController())
}
