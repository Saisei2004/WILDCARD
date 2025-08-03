package com.example.wildcard.ui.dashboard

import android.app.TimePickerDialog
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wildcard.R
import com.example.wildcard.data.model.User
import com.example.wildcard.ui.navigation.AppScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

private val MorningSkyBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFFFE0B2))
)
private val NightSkyWithGroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF0D47A1),
        0.7f to Color(0xFF1976D2),
        1.0f to Color(0xFF4A2300)
    )
)
private val ButtonBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
    radius = 400f
)
private val ButtonTextColor = Color.White
private val DarkTextColor = Color(0xFF263238)

@Composable
fun DashboardScreen(
    navController: NavController,
    roomId: String,
    applicationContext: Context
) {
    val viewModel: DashboardViewModel = viewModel(
        key = roomId,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                    return DashboardViewModel(roomId, applicationContext) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val room by viewModel.room.collectAsState()
    val users by viewModel.users.collectAsState()
    val isMorningPhase by viewModel.isMorningPhase.collectAsState()
    val currentUserStatus by viewModel.currentUserStatus.collectAsState()

    val context = LocalContext.current

    // オーディオマネージャ（アラーム音量バックアップ用）
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    var originalAlarmVolume by remember { mutableStateOf<Int?>(null) }

    // バイブレーション
    val vibrator = remember {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    }
    val shouldAlert = isMorningPhase && currentUserStatus != "woke_up"

    // MediaPlayer（alarm.mp3 は res/raw/alarm.mp3 前提）
    val mediaPlayer = remember {
        MediaPlayer().apply {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                }
                val afd = context.resources.openRawResourceFd(R.raw.alarm)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                setVolume(1f, 1f)
            } catch (_: Exception) {
                // silent fail
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            originalAlarmVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
            }
        }
    }

    LaunchedEffect(shouldAlert) {
        if (shouldAlert) {
            if (originalAlarmVolume == null) {
                originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    0
                )
            }

            vibrator?.takeIf { it.hasVibrator() }?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attrs = android.os.VibrationAttributes.Builder()
                            .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                            .build()
                        v.vibrate(effect, attrs)
                    } else {
                        v.vibrate(effect)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 500, 500), 0)
                }
            }

            if (!mediaPlayer.isPlaying) {
                try {
                    mediaPlayer.start()
                } catch (_: Exception) {
                }
            }
        } else {
            vibrator?.cancel()
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            }
            originalAlarmVolume?.let {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0)
            }
            originalAlarmVolume = null
        }
    }

    Crossfade(targetState = isMorningPhase, animationSpec = tween(1500), label = "PhaseChange") { isMorning ->
        val backgroundBrush = if (isMorning) MorningSkyBrush else NightSkyWithGroundBrush
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            if (!isMorning) {
                StarsAnimation()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (room == null) {
                    Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isMorning) {
                            MorningPhaseContent(viewModel = viewModel, currentUserStatus = currentUserStatus)
                        } else {
                            NightPhaseContent(viewModel = viewModel)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "参加者リスト",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(users) { user ->
                                val isPunishmentTime = (room?.wakeupTime ?: 0) < System.currentTimeMillis()
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                UserListItem(
                                    user = user,
                                    isPunishmentTime = isPunishmentTime,
                                    isClickable = isPunishmentTime && user.status != "woke_up" && user.uid != currentUserId,
                                    onUserClick = { selectedUser ->
                                        navController.navigate(AppScreen.RemoteControl.createRoute(selectedUser.uid))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NightPhaseContent(viewModel: DashboardViewModel) {
    val room by viewModel.room.collectAsState()
    val countdownHoursMinutes by viewModel.countdownHoursMinutes.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> viewModel.setWakeupTime(hourOfDay, minute) },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    Text(
        text = "起床予定時間",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )

    val wakeupTimeFormatted = room?.wakeupTime?.takeIf { it > 0 }?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "--:--"
    Text(
        text = wakeupTimeFormatted,
        fontSize = 90.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (room?.wakeupTime == 0L) "時間を設定してください" else "起床まで $countdownHoursMinutes",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))

    // デバッグボタンを追加
    Row(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LargeButton(text = "時間変更", onClick = { timePicker.show() }, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedButton(
            onClick = { showResetDialog = true },
            shape = CircleShape,
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(2.dp, Color.White)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "時間をリセット")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 朝フェーズへ移動（デバッグ）
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LargeButton(
            text = "朝フェーズへ移動",
            onClick = { viewModel.forceMorningPhase() },
            modifier = Modifier.width(220.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("確認") },
            text = { Text("起床時間をリセットしますか？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetWakeupTime()
                    showResetDialog = false
                }) { Text("リセット") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("キャンセル") }
            }
        )
    }
}

@Composable
private fun MorningPhaseContent(viewModel: DashboardViewModel, currentUserStatus: String) {
    val room by viewModel.room.collectAsState()

    Text(
        text = "起床予定時間",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTextColor
    )

    val wakeupTimeFormatted = room?.wakeupTime?.takeIf { it > 0 }?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "--:--"
    Text(
        text = wakeupTimeFormatted,
        fontSize = 90.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTextColor
    )

    Spacer(modifier = Modifier.height(24.dp))

    Box(modifier = Modifier.height(84.dp), contentAlignment = Alignment.Center) {
        if (currentUserStatus == "woke_up") {
            Text(
                text = "おはようございます！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextColor
            )
        } else {
            LargeButton(
                text = "起きた！",
                onClick = { viewModel.onWakeUpClicked() },
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
private fun LargeButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
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
                text = text,
                color = ButtonTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    isPunishmentTime: Boolean,
    isClickable: Boolean,
    onUserClick: (User) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) {
                onUserClick(user)
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            user.username,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = DarkTextColor
        )
        val statusText = when {
            user.status == "woke_up" -> "起床"
            isPunishmentTime -> "未起床"
            else -> "待機中"
        }
        val statusColor = when {
            isClickable -> MaterialTheme.colorScheme.error
            user.status == "woke_up" -> Color(0xFF2E7D32)
            else -> Color.Gray
        }
        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun StarsAnimation() {
    val stars = remember {
        List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                alpha = Animatable(0f)
            )
        }
    }

    LaunchedEffect(Unit) {
        stars.forEach { star ->
            launch {
                star.alpha.animateTo(
                    targetValue = Random.nextFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = (Random.nextFloat() * 3000 + 1000).toInt(),
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.White.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 100f
            ),
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            radius = 100f
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            radius = 50f
        )

        stars.forEach { star ->
            drawCircle(
                color = Color.White,
                center = Offset(star.x * size.width, star.y * size.height * 0.7f),
                radius = Random.nextFloat() * 2f + 1f,
                alpha = star.alpha.value
            )
        }
    }
}

private data class Star(val x: Float, val y: Float, val alpha: Animatable<Float, AnimationVector1D>)
