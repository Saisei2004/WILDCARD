package com.example.wildcard.ui.dashboard

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.example.wildcard.service.ev3.Ev3Controller
import com.example.wildcard.service.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(private val roomId: String) : ViewModel() {

    private val firebaseService = FirebaseService(FirebaseFirestore.getInstance())
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _countdown = MutableStateFlow("00:00:00")
    val countdown: StateFlow<String> = _countdown.asStateFlow()

    private val _showMissionButton = MutableStateFlow(false)
    val showMissionButton: StateFlow<Boolean> = _showMissionButton.asStateFlow()

    val currentUserStatus: StateFlow<String> = users.map { userList ->
        userList.find { it.uid == firebaseAuth.currentUser?.uid }?.status ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private var countDownTimer: CountDownTimer? = null
    private val ev3Controller = Ev3Controller()
    private var previousCommand = ControlCommand()

    init {
        // ✅ Checkpoint 1: ViewModelが初期化されたか
        Log.d("EV3_DEBUG", "DashboardViewModel initialized for roomId: $roomId")

        viewModelScope.launch {
            firebaseService.listenToRoomUpdates(roomId).collect { roomData ->
                _room.value = roomData
                roomData?.let { setupCountdown(it.wakeupTime) }
            }
        }
        viewModelScope.launch {
            firebaseService.listenToUsersInRoom(roomId).collect { userList ->
                _users.value = userList
            }
        }
        listenToEv3Commands()
    }

    private fun listenToEv3Commands() {
        val userId = firebaseAuth.currentUser?.uid
        // ✅ Checkpoint 2: コマンド監視を開始しようとしているか
        Log.d("EV3_DEBUG", "Attempting to listen for commands for userId: $userId")

        if (userId == null) {
            Log.e("EV3_DEBUG", "Cannot listen for commands because user is not logged in.")
            return
        }

        viewModelScope.launch {
            firebaseService.listenToCommands(userId).collect { newCommand ->
                // ✅ Checkpoint 3: Firebaseから新しいコマンドを受信したか
                Log.d("EV3_DEBUG", "New command received from Firebase: $newCommand")

                if (newCommand.direction != previousCommand.direction) {
                    val endpoint = when (newCommand.direction) {
                        "forward", "backward", "left", "right" -> "/${newCommand.direction}"
                        else -> "/stop"
                    }
                    ev3Controller.sendRequest(endpoint)
                }
                if (newCommand.action != previousCommand.action) {
                    val endpoint = when (newCommand.action) {
                        "hammer_strike" -> "/hammer/start"
                        else -> "/hammer/stop"
                    }
                    ev3Controller.sendRequest(endpoint)
                }
                if (newCommand.sound != previousCommand.sound) {
                    val endpoint = when (newCommand.sound) {
                        "alarm" -> "/beep/start"
                        else -> "/beep/stop"
                    }
                    ev3Controller.sendRequest(endpoint)
                }
                previousCommand = newCommand
            }
        }
    }

    private fun setupCountdown(wakeupTime: Long) {
        countDownTimer?.cancel()
        val remainingTime = wakeupTime - System.currentTimeMillis()

        if (remainingTime > 0) {
            _showMissionButton.value = false
            countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                    _countdown.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    _countdown.value = "時間です！"
                    _showMissionButton.value = true
                }
            }.start()
        } else {
            _countdown.value = "設定時間超過"
            _showMissionButton.value = true
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
            firebaseService.updateWakeupTime(roomId, newTime)
        }
    }

    fun onWakeUpClicked() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            firebaseService.updateUserStatus(userId, "woke_up")
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        ev3Controller.sendRequest("/stop")
    }
}
