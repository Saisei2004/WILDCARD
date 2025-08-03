package com.example.wildcard.ui.dashboard

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.example.wildcard.data.model.User
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val room by viewModel.room.collectAsState()
    val users by viewModel.users.collectAsState()
    // [追加] ViewModelから新しい状態を受け取る
    val showMissionButton by viewModel.showMissionButton.collectAsState()
    val currentUserStatus by viewModel.currentUserStatus.collectAsState()

    val context = LocalContext.current
    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> viewModel.setWakeupTime(hourOfDay, minute) },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // [変更] 中央のコンテンツ用に参照を一つにまとめる
        val (titleRef, timeRef, centerContentRef, listRef) = createRefs()
        val guideline20 = createGuidelineFromTop(0.2f)
        val guideline50 = createGuidelineFromTop(0.5f)

        Text(
            text = "起床時間",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(titleRef) {
                centerHorizontallyTo(parent)
                bottom.linkTo(guideline20)
            }
        )

        val wakeupTimeFormatted = room?.wakeupTime?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "未設定"
        Text(
            text = wakeupTimeFormatted,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(timeRef) {
                centerHorizontallyTo(parent)
                top.linkTo(titleRef.bottom)
            }
        )

        // [変更] カウントダウン、設定ボタン、「起きた！」ボタンを１つのBoxにまとめ、表示を切り替える
        Box(
            modifier = Modifier.constrainAs(centerContentRef) {
                top.linkTo(timeRef.bottom, margin = 8.dp)
                bottom.linkTo(guideline50)
                centerHorizontallyTo(parent)
                width = Dimension.fillToConstraints
            },
            contentAlignment = Alignment.Center
        ) {
            when {
                // 自分が起床済みの場合
                currentUserStatus == "woke_up" -> {
                    Text(
                        text = "おはようございます！",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // 時間になり、まだ起きていない場合
                showMissionButton -> {
                    Button(
                        onClick = { viewModel.onWakeUpClicked() },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                    ) {
                        Text("起きた！", fontSize = 18.sp)
                    }
                }
                // 起床時間になる前
                else -> {
                    val countdown by viewModel.countdown.collectAsState()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = countdown,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { timePicker.show() }) {
                            Text("起床時間を設定する")
                        }
                    }
                }
            }
        }

        // 参加者リストエリアは変更なし
        Column(
            modifier = Modifier.constrainAs(listRef) {
                top.linkTo(guideline50)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("参加者リスト", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(users) { user ->
                    val isPunishmentTime = (room?.wakeupTime ?: 0) < System.currentTimeMillis()
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    UserListItem(
                        user = user,
                        // [変更] 自分自身はクリックできないようにする
                        isClickable = isPunishmentTime && user.status != "woke_up" && user.uid != currentUserId,
                        onUserClick = { selectedUser ->
                            navController.navigate("remote_control_route/${selectedUser.uid}")
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun UserListItem(
    user: User,
    isClickable: Boolean,
    onUserClick: (User) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) {
                onUserClick(user)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.username, modifier = Modifier.weight(1f))
        Text(
            text = when (user.status) {
                "waiting" -> "待機中"
                "mission" -> "ミッション中"
                "woke_up" -> "起床済み"
                else -> ""
            },
            color = when {
                isClickable -> MaterialTheme.colorScheme.error
                user.status == "woke_up" -> MaterialTheme.colorScheme.primary
                else -> LocalContentColor.current
            }
        )
    }
}