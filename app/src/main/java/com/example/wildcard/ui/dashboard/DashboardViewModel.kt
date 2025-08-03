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
import java.util.*

// コンストラクタでroomIdを受け取るように変更
class DashboardViewModel(private val roomId: String) : ViewModel() {

    private val repository = FirebaseRepository()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _countdown = MutableStateFlow("00:00:00")
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    init {
        // 受け取ったroomIdを使って監視を開始
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
            countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
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
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val newTime = calendar.timeInMillis
        viewModelScope.launch {
            repository.updateWakeupTime(roomId, newTime)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}