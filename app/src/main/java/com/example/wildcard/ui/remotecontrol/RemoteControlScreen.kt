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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer

private val MorningSkyBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFFFE0B2))
)
private val ButtonBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
    radius = 200f
)
private val ButtonTextColor = Color.White
private val AccentColor = Color(0xFF2E7D32)
private val DarkTextColor = Color(0xFF263238)

/**
 * 遠隔操作画面（操作パネルの D-pad とアクションのバランス調整済み）
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
            verticalArrangement = Arrangement.Top
        ) {
            // カメラ映像
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
                    Text("Connecting.", color = Color.White, modifier = Modifier.padding(top = 80.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 操作パネル（サイズバランス調整済み）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DpadController(
                    onDirectionChange = viewModel::updateMoveDirection,
                    onRelease = viewModel::stopMovingIfNecessary,
                    baseSize = 180.dp,          // 少し控えめに
                    innerButtonSize = 64.dp     // ボタンもバランスを下げた
                )
                ActionController(
                    onPunishment = viewModel::updatePunishmentActive,
                    onSoundToggle = viewModel::updateSoundActive,
                    punishmentButtonSize = 100.dp // D-padとの比を揃えるため少し縮めたが存在感は維持
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

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
            onClick = { /* 長押しでのみ動く */ },
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

@Composable
private fun DpadController(
    onDirectionChange: (String) -> Unit,
    onRelease: (String) -> Unit,
    baseSize: androidx.compose.ui.unit.Dp = 160.dp,
    innerButtonSize: androidx.compose.ui.unit.Dp = 56.dp
) {
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
                .size(innerButtonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowUpward, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("left") },
            onRelease = { onRelease("left") },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(innerButtonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowBack, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("right") },
            onRelease = { onRelease("right") },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(innerButtonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowForward, isPressed = isPressed)
        }
        PressAndHoldButton(
            onPress = { onDirectionChange("backward") },
            onRelease = { onRelease("backward") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(innerButtonSize),
            shape = buttonShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.ArrowDownward, isPressed = isPressed)
        }
    }
}

@Composable
private fun ActionController(
    onPunishment: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    punishmentButtonSize: androidx.compose.ui.unit.Dp = 88.dp
) {
    var soundEnabled by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PressAndHoldButton(
            onPress = { onPunishment(true) },
            onRelease = { onPunishment(false) },
            modifier = Modifier.size(punishmentButtonSize),
            shape = CircleShape
        ) { isPressed ->
            IconWithShadow(icon = Icons.Default.Bolt, isPressed = isPressed, size = 32.dp, text = "お仕置き")
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
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.5f),
                checkedIconColor = AccentColor,
                uncheckedIconColor = Color.Gray
            )
        )
    }
}

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
