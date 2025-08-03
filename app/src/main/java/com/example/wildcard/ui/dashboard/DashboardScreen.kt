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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val room by viewModel.room.collectAsState()
    val users by viewModel.users.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val context = LocalContext.current

    // TimePickerのロジックは変更なし
    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> viewModel.setWakeupTime(hourOfDay, minute) },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    // 【変更点】レイアウトの親をConstraintLayoutに変更
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // 左右のパディングのみ設定
    ) {
        // 配置する要素の参照を作成
        val (titleRef, timeRef, countdownRef, buttonRef, listRef) = createRefs()

        // 画面内の特定の位置を示す「ガイドライン」を作成
        val guideline20 = createGuidelineFromTop(0.2f) // 上から20%
        val guideline30 = createGuidelineFromTop(0.3f) // 上から30%
        val guideline40 = createGuidelineFromTop(0.4f) // 上から40%
        val guideline50 = createGuidelineFromTop(0.5f) // 上から50% (中央)

        // 「起床時間」ラベルを20%ラインに配置
        Text(
            text = "起床時間",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(titleRef) {
                centerHorizontallyTo(parent)
                bottom.linkTo(guideline20)
            }
        )

        // 起床時間（00:00）をラベルの下に配置
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

        // カウントダウン表示を30%ラインに配置
        Text(
            text = countdown,
            fontSize = 32.sp,
            modifier = Modifier.constrainAs(countdownRef) {
                centerHorizontallyTo(parent)
                top.linkTo(guideline30)
                bottom.linkTo(guideline30)
            }
        )

        // 設定ボタンを40%ラインに配置
        Button(
            onClick = { timePicker.show() },
            modifier = Modifier.constrainAs(buttonRef) {
                centerHorizontallyTo(parent)
                top.linkTo(guideline40)
                bottom.linkTo(guideline40)
            }
        ) {
            Text("起床時間を設定する")
        }

        // 参加者リストエリアを画面下半分に配置
        Column(
            modifier = Modifier.constrainAs(listRef) {
                top.linkTo(guideline50) // 50%ラインから開始
                bottom.linkTo(parent.bottom) // 画面下部まで
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints // 幅を親に合わせる
                height = Dimension.fillToConstraints // 高さを制約に合わせて埋める
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("参加者リスト", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(users) { user ->
                    val isPunishmentTime = (room?.wakeupTime ?: 0) < System.currentTimeMillis()
                    UserListItem(
                        user = user,
                        isClickable = isPunishmentTime && user.status != "woke_up",
                        onUserClick = { selectedUser ->
                            navController.navigate("remote_control_route/${selectedUser.uid}")
                        }
                    )
                }
            }
        }
    }
}

// UserListItem関数は変更なし
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