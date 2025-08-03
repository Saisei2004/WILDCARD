package com.example.wildcard.ui.remotecontrol

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

// --- 新しいカラーパレット (深緑テーマ) ---
private val MorningSkyBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFFFE0B2))
)
private val ButtonBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)), // 深緑のグラデーション
    radius = 200f
)
private val ButtonTextColor = Color.White
private val AccentColor = Color(0xFF2E7D32) // 深緑
private val DarkTextColor = Color(0xFF263238)

/**
 * 遠隔操作画面 (新UIデザイン v2.2)
 *
 * 深緑と朝焼けをテーマにした、高級感のあるゲームUIデザイン。
 * 片手操作を意識して、よりコンパクトなレイアウトに調整。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    navController: NavController,
    targetUserId: String
) {
    val context = LocalContext.current
    val viewModel: RemoteControlViewModel = viewModel(
        factory = RemoteControlViewModelFactory(targetUserId, context.applicationContext)
    )

    val remoteVideoStream = viewModel.remoteVideoStream
    var remoteRenderView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    LaunchedEffect(Unit) {
        viewModel.startViewing()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.endControl()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REMOTE CONTROL", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = DarkTextColor,
                    navigationIconContentColor = DarkTextColor
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(MorningSkyBrush)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // カメラ映像表示エリア
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (remoteVideoStream != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            SurfaceViewRenderer(ctx).apply {
                                setup()
                                remoteVideoStream.addRenderer(this)
                                remoteRenderView = this
                            }
                        },
                        update = { view ->
                            remoteVideoStream.removeRenderer(view)
                            remoteVideoStream.addRenderer(view)
                        }
                    )
                } else {
                    CircularProgressIndicator(color = AccentColor)
                    Text("Connecting...", color = Color.White, modifier = Modifier.padding(top = 80.dp))
                }
            }

            // 操作パネル
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DpadController(
                    onDirectionChange = viewModel::updateMoveDirection,
                    onRelease = viewModel::stopMovingIfNecessary
                )
                ActionController(
                    onPunishment = viewModel::updatePunishmentActive,
                    onSoundToggle = viewModel::updateSoundActive
                )
            }

            // 操作終了ボタン
            Button(
                onClick = {
                    viewModel.endControl()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    "操作終了",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DarkTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
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
    shape: Shape = ButtonDefaults.shape,
    content: @Composable (isPressed: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    onPress()
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    isPressed = false
                    onRelease()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .background(brush = ButtonBrush, shape = shape)
            .clip(shape)
            .then(
                Modifier.border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = shape
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { /* 長押し処理はLaunchedEffectが担当 */ },
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = ButtonTextColor
            ),
            interactionSource = interactionSource,
            contentPadding = PaddingValues(0.dp)
        ) {
            content(isPressed)
        }
    }
}

/**
 * 十字キーコントローラー
 */
@Composable
private fun DpadController(onDirectionChange: (String) -> Unit, onRelease: (String) -> Unit) {
    val baseSize = 160.dp
    val buttonSize = 56.dp

    Box(
        modifier = Modifier
            .size(baseSize)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .background(color = Color.White.copy(alpha = 0.5f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val buttonShape = CircleShape

        PressAndHoldButton(
            onPress = { onDirectionChange("forward") },
            onRelease = { onRelease("forward") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(buttonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowUpward, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("left") },
            onRelease = { onRelease("left") },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(buttonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowBack, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("right") },
            onRelease = { onRelease("right") },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(buttonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowForward, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("backward") },
            onRelease = { onRelease("backward") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(buttonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowDownward, isPressed = isPressed)
        }
    }
}

/**
 * アクションボタン（お仕置き、音）のコントローラー
 */
@Composable
private fun ActionController(
    onPunishment: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit
) {
    var soundEnabled by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PressAndHoldButton(
            onPress = { onPunishment(true) },
            onRelease = { onPunishment(false) },
            modifier = Modifier.size(88.dp),
            shape = CircleShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.Bolt, isPressed = isPressed, size = 36.dp, text = "お仕置き")
        }

        Switch(
            checked = soundEnabled,
            onCheckedChange = {
                soundEnabled = it
                onSoundToggle(it)
            },
            thumbContent = {
                Icon(
                    if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentColor, // 深緑に合わせる
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.5f),
                checkedIconColor = AccentColor,
                uncheckedIconColor = Color.Gray
            )
        )
    }
}

/**
 * 押下時に影がつくアイコン
 */
@Composable
private fun IconWithShadow(icon: ImageVector, isPressed: Boolean, size: androidx.compose.ui.unit.Dp = 24.dp, text: String? = null) {
    val elevation by animateDpAsState(if (isPressed) 2.dp else 8.dp, label = "elevation")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = (-elevation / 2))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ButtonTextColor,
            modifier = Modifier
                .size(size)
                .shadow(elevation = elevation, shape = CircleShape)
        )
        if (text != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = ButtonTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ViewModelFactory (変更なし)
class RemoteControlViewModelFactory(
    private val targetUserId: String,
    private val applicationContext: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemoteControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemoteControlViewModel(targetUserId, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewRemoteControlScreen() {
    MaterialTheme {
        RemoteControlScreen(rememberNavController(), "dummyUserId")
    }
}
