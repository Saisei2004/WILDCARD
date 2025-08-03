package com.example.wildcard.ui.dashboard

import android.app.TimePickerDialog
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

    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> viewModel.setWakeupTime(hourOfDay, minute) },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("起床時間", style = MaterialTheme.typography.titleMedium)
        val wakeupTimeFormatted = room?.wakeupTime?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "未設定"
        Text(wakeupTimeFormatted, fontSize = 48.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(countdown, fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { timePicker.show() }) {
            Text("起床時間を設定する")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("参加者リスト", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(users) { user ->
                UserListItem(user)
            }
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            color = when (user.status) {
                "woke_up" -> MaterialTheme.colorScheme.primary
                else -> LocalContentColor.current
            }
        )
    }
}