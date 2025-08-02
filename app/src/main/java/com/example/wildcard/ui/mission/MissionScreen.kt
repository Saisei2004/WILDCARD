package com.example.wildcard.ui.mission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * 起床ミッション画面
 *
 * スマートフォンのインカメラを使用して、10秒間目を開け続けるミッションを実行します。
 * ミッションの進捗を表示し、成功または失敗に応じて適切な画面へ遷移します。
 */
@Composable
fun MissionScreen(navController: NavController) {
    // TODO: ImageRecognitionService を使用して目の開閉を検出し、ミッションの進捗を管理
    var missionProgress by remember { mutableStateOf(0) } // 0-100で進捗を表す
    var missionStatus by remember { mutableStateOf("ミッション開始！") } // ミッションの状態メッセージ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = missionStatus,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // TODO: カメラプレビューを表示するコンポーネントをここに配置
        // AndroidView(factory = { context ->
        //    // CameraX PreviewViewなどを設定
        //    PreviewView(context).apply {
        //        // ...
        //    }
        //})
        Box(modifier = Modifier
            .size(200.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(androidx.compose.ui.graphics.Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("カメラプレビュー", color = androidx.compose.ui.graphics.Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 進捗バー
        LinearProgressIndicator(
            progress = missionProgress / 100f,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("進捗: $missionProgress%")

        Spacer(modifier = Modifier.height(32.dp))

        // TODO: ミッション成功/失敗時のナビゲーションロジック
        // 仮のボタン
        Button(onClick = {
            // ミッション成功の仮処理
            missionStatus = "ミッション成功！"
            // TODO: FirebaseService を通じて自身のステータスを「起床済み」に更新
            // TODO: EV3へアラーム停止信号を送信
            navController.navigate("dashboard_route") { popUpTo("dashboard_route") { inclusive = true } } // ダッシュボードに戻る
        }) {
            Text("ミッション成功 (仮)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // ミッション失敗の仮処理
            missionStatus = "ミッション失敗..."
            navController.navigate("dashboard_route") { popUpTo("dashboard_route") { inclusive = true } } // ダッシュボードに戻る
        }) {
            Text("ミッション失敗 (仮)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMissionScreen() {
    MissionScreen(rememberNavController())
}
