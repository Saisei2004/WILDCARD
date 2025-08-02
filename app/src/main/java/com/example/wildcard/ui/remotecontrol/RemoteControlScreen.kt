package com.example.wildcard.ui.remotecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * 遠隔操作画面
 *
 * 未起床のメンバーのEV3ロボットを遠隔操作するための画面です。
 * 対象のEV3に搭載されたスマートフォンのカメラ映像を表示し、
 * バーチャルコントローラーと特定のアクションを実行するボタンを提供します。
 */
@Composable
fun RemoteControlScreen(navController: NavController) {
    // TODO: CallManager を使用して対象のEV3搭載スマホからの映像ストリームを表示
    // TODO: RobotController を使用してEV3への操作コマンドを送信

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EV3遠隔操作",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // カメラ映像表示エリア
        Box(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // 16:9のアスペクト比を想定
            .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("対象のEV3カメラ映像", color = Color.White)
            // TODO: WebRTCのSurfaceViewRendererなどをここに統合
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 操作パネル（十字キーなど）
        Text("操作パネル", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Button(onClick = { /* TODO: 前進コマンド送信 */ }) { Text("↑") }
            Column {
                Button(onClick = { /* TODO: 左旋回コマンド送信 */ }) { Text("←") }
                Button(onClick = { /* TODO: 右旋回コマンド送信 */ }) { Text("→") }
            }
            Button(onClick = { /* TODO: 後退コマンド送信 */ }) { Text("↓") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // お仕置きボタン
        Button(
            onClick = { /* TODO: お仕置きアクションコマンド送信 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("お仕置き！")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("操作終了")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRemoteControlScreen() {
    RemoteControlScreen(rememberNavController())
}
