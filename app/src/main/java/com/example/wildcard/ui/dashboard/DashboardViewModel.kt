package com.example.wildcard.ui.dashboard

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.FirebaseRepository
import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _countdown = MutableStateFlow("00:00:00")
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            // 現在のユーザー情報を取得し、そのユーザーが属するルームの監視を開始する
            val currentUser = repository.getCurrentUser()
            currentUser?.roomId?.let { roomId ->
                repository.listenToRoomUpdates(roomId).collect { roomData ->
                    _room.value = roomData
                    // 起床時間が更新されたらカウントダウンを再設定
                    setupCountdown(roomData.wakeupTime)
                }
            }
            currentUser?.roomId?.let { roomId ->
                repository.listenToUsersInRoom(roomId).collect { userList ->
                    _users.value = userList
                }
            }
        }
    }

    private fun setupCountdown(wakeupTime: Long) {
        countDownTimer?.cancel() // 既存のタイマーはキャンセル
        val remainingTime = wakeupTime - System.currentTimeMillis()

        if (remainingTime > 0) {
            countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // HH:mm:ss形式にフォーマット
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                    _countdown.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    _countdown.value = "時間です！"
                    // TODO: アラームを鳴らす処理をここに実装
                }
            }.start()
        } else {
            _countdown.value = "設定時間超過"
        }
    }

    fun setWakeupTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // もし設定した時刻が現在時刻より前なら、明日の時刻として設定
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val newTime = calendar.timeInMillis
        viewModelScope.launch {
            _room.value?.roomCode?.let { roomId ->
                repository.updateWakeupTime(roomId, newTime)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel() // ViewModelが破棄されるときにタイマーも止める
    }
}