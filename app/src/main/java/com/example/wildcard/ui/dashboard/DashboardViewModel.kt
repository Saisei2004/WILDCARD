package com.example.wildcard.ui.dashboard

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.example.wildcard.service.ev3.Ev3Controller
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.webrtc.CallManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    private val roomId: String,
    private val applicationContext: Context? = null
) : ViewModel() {

    private val firebaseService = FirebaseService(FirebaseFirestore.getInstance())
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val callManager = applicationContext?.let { CallManager(it) }

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _countdownHoursMinutes = MutableStateFlow("00:00")
    val countdownHoursMinutes: StateFlow<String> = _countdownHoursMinutes.asStateFlow()

    // 通常の朝フェーズ判定（時間ベース）
    private val _computedMorningPhase = MutableStateFlow(false)
    // 強制的に朝フェーズにするオーバーライド（デバッグ用）
    private val _forcedMorning = MutableStateFlow(false)
    // 実際に使う朝フェーズ（通常 OR 強制）
    val isMorningPhase: StateFlow<Boolean> = combine(
        _computedMorningPhase,
        _forcedMorning
    ) { computed, forced -> computed || forced }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserStatus: StateFlow<String> = users.map { userList ->
        userList.find { it.uid == firebaseAuth.currentUser?.uid }?.status ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private var countDownTimer: CountDownTimer? = null
    private val ev3Controller = Ev3Controller()
    private var previousCommand = ControlCommand()

    init {
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
        viewModelScope.launch {
            isMorningPhase.collect { shouldStartPublishing ->
                if (shouldStartPublishing && !_isPublishing.value) {
                    startPublishing()
                }
            }
        }
    }

    private fun listenToEv3Commands() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            firebaseService.listenToCommands(userId).collect { newCommand ->
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
        val currentTime = System.currentTimeMillis()
        val remainingTime = wakeupTime - currentTime
        val oneHourInMillis = 3600 * 1000

        _computedMorningPhase.value =
            wakeupTime > 0 && currentTime >= wakeupTime && currentTime < (wakeupTime + oneHourInMillis)

        if (remainingTime > 0) {
            countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    _countdownHoursMinutes.value = String.format("%02d:%02d", hours, minutes)

                    val now = System.currentTimeMillis()
                    _computedMorningPhase.value =
                        wakeupTime > 0 && now >= wakeupTime && now < (wakeupTime + oneHourInMillis)
                }

                override fun onFinish() {
                    _countdownHoursMinutes.value = "00:00"
                    _computedMorningPhase.value = true
                }
            }.start()
        } else {
            _countdownHoursMinutes.value = "00:00"
        }
    }

    fun setWakeupTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        val newTime = calendar.timeInMillis
        viewModelScope.launch {
            firebaseService.updateWakeupTime(roomId, newTime)
        }
    }

    fun resetWakeupTime() {
        viewModelScope.launch {
            firebaseService.updateWakeupTime(roomId, 0L)
        }
    }

    fun onWakeUpClicked() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            firebaseService.updateUserStatus(userId, "woke_up")
            stopPublishing()
        }
    }

    /** デバッグ用：強制的に朝フェーズに移す */
    fun forceMorningPhase() {
        _forcedMorning.value = true
    }

    private fun startPublishing() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (callManager == null) return
        viewModelScope.launch {
            try {
                val roomName = "control_$userId"
                val success = callManager.startPublishing(roomName)
                if (success) _isPublishing.value = true
            } catch (_: Exception) {
                // no-op
            }
        }
    }

    private fun stopPublishing() {
        viewModelScope.launch {
            try {
                callManager?.endCall()
                _isPublishing.value = false
            } catch (_: Exception) {
                // no-op
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        ev3Controller.sendRequest("/stop")
        viewModelScope.launch {
            callManager?.endCall()
        }
    }
}
