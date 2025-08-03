package com.example.wildcard.ui.registration

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wildcard.ui.screens.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun UserRegistrationScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    // --- 1. UIの状態を管理する変数 ---
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- 2. ViewModelからのイベントを監視 ---
    //    (ルーム参加成功/失敗を待って画面遷移やトースト表示を行う)
    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeViewModel.NavigationEvent.NavigateToDashboard -> {
                    // 成功したらダッシュボードへ
                    navController.navigate("dashboard_route")
                }
                is HomeViewModel.NavigationEvent.ShowError -> {
                    // 失敗したらエラーメッセージを表示
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- 3. 画面のレイアウト ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("ユーザー名") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showDialog = true }) { // 「ルームに入る」ボタン
            Text("ルームに入る")
        }
    }

    // --- 4. 合言葉入力ダイアログの表示 ---
    //    (showDialogがtrueのときだけ表示される)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("合言葉の入力") },
            text = {
                TextField(
                    value = roomCode,
                    onValueChange = { roomCode = it },
                    label = { Text("合言葉") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // ViewModelの関数を呼び出す
                        scope.launch {
                            homeViewModel.joinRoom(username, roomCode)
                        }
                        showDialog = false // ダイアログを閉じる
                    },
                    // ユーザー名と合言葉が空でない場合のみボタンを有効化
                    enabled = username.isNotBlank() && roomCode.isNotBlank()
                ) {
                    Text("入室")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}