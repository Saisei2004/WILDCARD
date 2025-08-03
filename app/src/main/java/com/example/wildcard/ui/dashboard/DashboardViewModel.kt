package com.example.wildcard.ui.dashboard

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.FirebaseRepository
import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(private val roomId: String) : ViewModel() {

    private val repository = FirebaseRepository()
    // [追加] ユーザーを識別するためにFirebaseAuthのインスタンスを追加
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _countdown = MutableStateFlow("00:00:00")
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    // [追加] ボタン表示を制御するための状態
    private val _showMissionButton = MutableStateFlow(false)
    val showMissionButton: StateFlow<Boolean> = _showMissionButton.asStateFlow()

    // [追加] 現在のユーザーの状態を監視
    val currentUserStatus: StateFlow<String> = users.map { userList ->
        userList.find { it.uid == firebaseAuth.currentUser?.uid }?.status ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")


    private var countDownTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            repository.listenToRoomUpdates(roomId).collect { roomData ->
                _room.value = roomData
                setupCountdown(roomData.wakeupTime)
            }
        }
        viewModelScope.launch {
            repository.listenToUsersInRoom(roomId).collect { userList ->
                _users.value = userList
            }
        }
    }

    private fun setupCountdown(wakeupTime: Long) {
        countDownTimer?.cancel()
        val remainingTime = wakeupTime - System.currentTimeMillis()

        if (remainingTime > 0) {
            _showMissionButton.value = false // [変更] 時間設定時はボタンを非表示
            countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                    _countdown.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    _countdown.value = "時間です！"
                    _showMissionButton.value = true // [変更] 時間になったらボタンを表示
                }
            }.start()
        } else {
            _countdown.value = "設定時間超過"
            _showMissionButton.value = true // [変更] 既に時間を過ぎていたらボタンを表示
        }
    }

    fun setWakeupTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val newTime = calendar.timeInMillis
        viewModelScope.launch {
            repository.updateWakeupTime(roomId, newTime)
        }
    }

    // [変更] 「起きた！」ボタンが押された時の処理を実装
    fun onWakeUpClicked() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            // Firestoreのユーザー状態を "woke_up" に更新
            repository.updateUserStatus(userId, "woke_up")
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}