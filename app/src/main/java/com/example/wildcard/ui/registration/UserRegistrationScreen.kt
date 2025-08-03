package com.example.wildcard.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.domain.managers.RoomManager
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController

/**
 * ユーザー名登録とルーム参加画面
 *
 * ユーザーが自身のユーザー名を入力し、既存のルームに合言葉で参加するか、
 * 新しいルームを合言葉で作成して参加する機能を提供します。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showRegistrationError by remember { mutableStateOf(false) } // エラー表示用

    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val firebaseService = remember { FirebaseService(firestore) }
    val roomManager = remember { RoomManager(firebaseService) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ユーザー名入力フィールド
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("ユーザー名") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ルームに入るボタン
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ルームに入る")
        }

        // ルーム参加ダイアログ
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("ルームに参加") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = roomCode,
                            onValueChange = { roomCode = it },
                            label = { Text("合言葉") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (username.isBlank() || roomCode.isBlank()) {
                                showRegistrationError = true
                                return@Button
                            }
                            scope.launch {
                                val success = roomManager.registerUserAndJoinOrCreateRoom(username, roomCode)
                                if (success) {
                                    showDialog = false
                                    navController.navigate("dashboard_route") {
                                        popUpTo("dashboard_route") { inclusive = true }
                                    }
                                } else {
                                    showRegistrationError = true
                                }
                            }
                        }
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

        // エラー表示ダイアログ
        if (showRegistrationError) {
            AlertDialog(
                onDismissRequest = { showRegistrationError = false },
                title = { Text("エラー") },
                text = { Text("ユーザー名または合言葉が不正です。") },
                confirmButton = {
                    Button(onClick = { showRegistrationError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserRegistrationScreen() {
    UserRegistrationScreen(rememberNavController())
}
