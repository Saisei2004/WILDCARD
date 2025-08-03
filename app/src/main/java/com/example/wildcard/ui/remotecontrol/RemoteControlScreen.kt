
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
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
    val context = LocalContext.current
    val viewModel = remember { RemoteControlViewModel(targetUserId, context.applicationContext) }
    
    // WebRTC関連の状態
    val remoteVideoStream = viewModel.remoteVideoStream
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    
    // ViewModelの状態を監視
    val moveDirection = viewModel.moveDirection
    val punishmentActive = viewModel.punishmentActive
    val soundActive = viewModel.soundActive

    // 画面表示時にWebRTC視聴を開始
    LaunchedEffect(Unit) {
        viewModel.startViewing()
    }

    // 画面終了時にリソースを解放
    DisposableEffect(Unit) {
        onDispose {
            viewModel.endControl()
        }
    }

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
            if (remoteVideoStream != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        remoteRenderView = SurfaceViewRenderer(ctx)
                        remoteRenderView!!.apply {
                            setup()
                            remoteVideoStream.addRenderer(this)
                        }
                    },
                    update = {
                        remoteRenderView?.let { renderer ->
                            remoteVideoStream.removeRenderer(renderer)
                            remoteVideoStream.addRenderer(renderer)
                        }
                    }
                )
            } else {
                Text("映像接続中...", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 操作パネル（十字キーなど）
        Text("操作パネル", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 上ボタン
            PressAndHoldButton(
                onPress = { viewModel.updateMoveDirection("forward") },
                onRelease = { if (moveDirection == "forward") viewModel.updateMoveDirection("stop") }
            ) { Text("↑") }

            // 中央のボタン群
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PressAndHoldButton(
                    onPress = { viewModel.updateMoveDirection("left") },
                    onRelease = { if (moveDirection == "left") viewModel.updateMoveDirection("stop") }
                ) { Text("←") }

                Spacer(modifier = Modifier.width(70.dp)) // 見た目のための空白

                PressAndHoldButton(
                    onPress = { viewModel.updateMoveDirection("right") },
                    onRelease = { if (moveDirection == "right") viewModel.updateMoveDirection("stop") }
                ) { Text("→") }
            }

            // 下ボタン
            PressAndHoldButton(
                onPress = { viewModel.updateMoveDirection("backward") },
                onRelease = { if (moveDirection == "backward") viewModel.updateMoveDirection("stop") }
            ) { Text("↓") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // アクションボタン群
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // お仕置きボタン
            PressAndHoldButton(
                onPress = { viewModel.updatePunishmentActive(true) },
                onRelease = { viewModel.updatePunishmentActive(false) },
                modifier = Modifier.weight(1f)
            ) {
                Text("お仕置き！")
            }
            // 音を鳴らすボタン
            PressAndHoldButton(
                onPress = { viewModel.updateSoundActive(true) },
                onRelease = { viewModel.updateSoundActive(false) },
                modifier = Modifier.weight(1f)
            ) {
                Text("音を鳴らす")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { 
            viewModel.endControl()
            navController.popBackStack() 
        }) {
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
