package com.example.wildcard.ui.registration

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wildcard.ui.navigation.AppScreen
import com.example.wildcard.R // Rクラスをインポートしてください
import com.example.wildcard.ui.screens.HomeViewModel
import kotlinx.coroutines.launch

// --- テーマカラーパレット ---
private val MorningSkyBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFFFE0B2))
)
private val ButtonBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)), // 深緑のグラデーション
    radius = 400f
)
private val ButtonTextColor = Color.White
private val DarkTextColor = Color(0xFF263238)
private val AccentColor = Color(0xFF2E7D32)

@Composable
fun UserRegistrationScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ViewModelからのナビゲーションイベントを監視
    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeViewModel.NavigationEvent.NavigateToDashboard -> {
                    navController.navigate(AppScreen.Dashboard.createRoute(event.roomId))
                }
                is HomeViewModel.NavigationEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- UI部分 ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MorningSkyBrush)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "logo-bounce")
        val logoOffsetY by infiniteTransition.animateValue(
            initialValue = (-10).dp,
            targetValue = 10.dp,
            typeConverter = Dp.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2500, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo-bounce-offset"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. ロゴ (上部のスペース)
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Sleep Buster Logo",
                modifier = Modifier
                    .size(280.dp)
                    .offset(y = logoOffsetY)
                    .shadow(elevation = 24.dp, shape = CircleShape),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.weight(0.5f))

            // 2. 入力フォームとボタン (中央に配置)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ユーザー名",
                    color = DarkTextColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentColor,
                    ),
                    textStyle = TextStyle(color = DarkTextColor, fontSize = 16.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (username.isNotBlank()) {
                            showDialog = true
                        } else {
                            Toast.makeText(context, "ユーザー名を入力してください", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ButtonBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ルームに入る",
                            color = ButtonTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
            // 3. 下部のスペース
            Spacer(modifier = Modifier.weight(1.5f))
        }
    }

    // ルームコード入力ダイアログ
    if (showDialog) {
        StyledAlertDialog(
            onDismissRequest = { showDialog = false },
            title = "合言葉の入力",
            roomCode = roomCode,
            onRoomCodeChange = { roomCode = it },
            onConfirm = {
                scope.launch {
                    homeViewModel.joinRoom(username, roomCode)
                }
                showDialog = false
            },
            isConfirmEnabled = username.isNotBlank() && roomCode.isNotBlank()
        )
    }
}

/**
 * カスタムスタイルを適用したAlertDialog
 */
@Composable
private fun StyledAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = DarkTextColor)
        },
        text = {
            TextField(
                value = roomCode,
                onValueChange = onRoomCodeChange, // ★ エラー修正: { roomCode = it } -> onRoomCodeChange
                label = { Text("合言葉") },
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = AccentColor,
                    cursorColor = AccentColor,
                ),
                textStyle = TextStyle(color = DarkTextColor, fontSize = 16.sp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text("入室", color = ButtonTextColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル", color = Color.Gray)
            }
        }
    )
}
