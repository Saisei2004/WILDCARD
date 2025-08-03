
package com.example.wildcard.ui.remotecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.service.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 遠隔操作画面
 *
 * 未起床のメンバーのEV3ロボットを遠隔操作するための画面です。
 * 対象のEV3に搭載されたスマートフォンのカメラ映像を表示し、
 * バーチャルコントローラーと特定のアクションを実行するボタンを提供します。
 */
@Composable
fun RemoteControlScreen(
    navController: NavController,
    targetUserId: String
) {
    // FirebaseServiceをインスタンス化
    val firebaseService = remember { FirebaseService(FirebaseFirestore.getInstance()) }
    // 非同期処理（Firebaseへの送信）を行うためのスコープ
    val scope = rememberCoroutineScope()

    // --- ここからが新しいロジック ---

    // 現在操作しているユーザー（送信者）のIDを取得
    val senderId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_sender" }

    // 現在の「移動方向」「お仕置き」「音」の状態を保持する変数
    var moveDirection by remember { mutableStateOf("stop") }
    var punishmentActive by remember { mutableStateOf(false) }
    var soundActive by remember { mutableStateOf(false) } // ✅ 音の状態を追加

    // いずれかの状態が変化するたびに、最新のコマンドを合成してFirebaseに送信するエフェクト
    LaunchedEffect(moveDirection, punishmentActive, soundActive) {
        val command = ControlCommand(
            senderId = senderId,
            direction = moveDirection,
            action = if (punishmentActive) "hammer_strike" else "none",
            sound = if (soundActive) "alarm" else "none" // ✅ 音の状態をコマンドに含める
        )
        scope.launch {
            firebaseService.sendControlCommand(targetUserId, command)
        }
    }

    // --- ここまでが新しいロジック ---


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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("対象のEV3カメラ映像", color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 操作パネル（十字キーなど）
        Text("操作パネル", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 上ボタン
            PressAndHoldButton(
                onPress = { moveDirection = "forward" },
                onRelease = { if (moveDirection == "forward") moveDirection = "stop" }
            ) { Text("↑") }

            // 中央のボタン群
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PressAndHoldButton(
                    onPress = { moveDirection = "left" },
                    onRelease = { if (moveDirection == "left") moveDirection = "stop" }
                ) { Text("←") }

                Spacer(modifier = Modifier.width(70.dp)) // 見た目のための空白

                PressAndHoldButton(
                    onPress = { moveDirection = "right" },
                    onRelease = { if (moveDirection == "right") moveDirection = "stop" }
                ) { Text("→") }
            }

            // 下ボタン
            PressAndHoldButton(
                onPress = { moveDirection = "backward" },
                onRelease = { if (moveDirection == "backward") moveDirection = "stop" }
            ) { Text("↓") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ アクションボタン群
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // ボタン間のスペース
        ) {
            // お仕置きボタン
            PressAndHoldButton(
                onPress = { punishmentActive = true },
                onRelease = { punishmentActive = false },
                modifier = Modifier.weight(1f) // 横幅を均等に分ける
            ) {
                Text("お仕置き！")
            }
            // 音を鳴らすボタン
            PressAndHoldButton(
                onPress = { soundActive = true },
                onRelease = { soundActive = false },
                modifier = Modifier.weight(1f) // 横幅を均等に分ける
            ) {
                Text("音を鳴らす")
            }
        }


        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("操作終了")
        }
    }
}

/**
 * 押下・解放を検知する共通ボタン
 */
@Composable
private fun PressAndHoldButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> onPress()
                is PressInteraction.Release, is PressInteraction.Cancel -> onRelease()
            }
        }
    }

    Button(
        onClick = { /* 押下・解放の処理はLaunchedEffectが担当 */ },
        interactionSource = interactionSource,
        modifier = modifier,
        content = content
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewRemoteControlScreen() {
    RemoteControlScreen(rememberNavController(), "dummyUserId")
}
