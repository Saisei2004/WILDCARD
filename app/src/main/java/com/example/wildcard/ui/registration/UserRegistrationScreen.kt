package com.example.wildcard.ui.registration

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wildcard.ui.screens.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeViewModel.NavigationEvent.NavigateToDashboard -> {
                    navController.navigate("dashboard_route/${event.roomId}")
                }
                is HomeViewModel.NavigationEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val (titleRef, formRef) = createRefs()

        // [変更] タイトル用のガイドラインを上から30%の位置に修正
        val titleGuideline = createGuidelineFromTop(0.3f)
        // [変更] 入力フォーム用のガイドラインを上から55%（中央より少し下）の位置に作成
        val formGuideline = createGuidelineFromTop(0.55f)

        // タイトルを30%の位置に配置
        Text(
            text = "コケコッコー",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(titleRef) {
                centerHorizontallyTo(parent)
                // [変更] 参照するガイドラインを変更
                top.linkTo(titleGuideline)
                bottom.linkTo(titleGuideline)
            }
        )

        // 入力フォームを55%の位置（中央より少し下）に配置
        Column(
            modifier = Modifier.constrainAs(formRef) {
                centerHorizontallyTo(parent)
                // [変更] 参照するガイドラインを変更
                top.linkTo(formGuideline)
                bottom.linkTo(formGuideline)
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("ユーザー名") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showDialog = true }) {
                Text("ルームに入る")
            }
        }
    }

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
                        scope.launch {
                            homeViewModel.joinRoom(username, roomCode)
                        }
                        showDialog = false
                    },
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