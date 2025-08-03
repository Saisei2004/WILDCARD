package com.example.wildcard.ui.remotecontrol

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.webrtc.CallManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import kotlinx.coroutines.launch

/**
 * 遠隔操作画面のViewModel
 * WebRTC映像受信（映像のみ）とコントロールコマンド送信を管理します。
 */
class RemoteControlViewModel(
    private val targetUserId: String,
    private val applicationContext: Context
) : ViewModel() {

    private val firebaseService = FirebaseService(FirebaseFirestore.getInstance())
    private val callManager = CallManager(applicationContext)

    // WebRTC関連の状態
    val remoteVideoStream: RemoteVideoStream? get() = callManager.remoteVideoStream
    
    // コントロール関連の状態
    var moveDirection by mutableStateOf("stop")
        private set
    var punishmentActive by mutableStateOf(false)
        private set
    var soundActive by mutableStateOf(false)
        private set

    // 現在操作しているユーザーのID
    private val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_sender"

    /**
     * WebRTC視聴（映像のみ）を開始します
     */
    fun startViewing() {
        viewModelScope.launch {
            val roomName = "control_$targetUserId"
            callManager.startViewing(roomName)
        }
    }

    /**
     * 移動方向を設定します
     */
    fun updateMoveDirection(direction: String) {
        moveDirection = direction
        sendControlCommand()
    }

    /**
     * お仕置き状態を設定します
     */
    fun updatePunishmentActive(active: Boolean) {
        punishmentActive = active
        sendControlCommand()
    }

    /**
     * 音の状態を設定します
     */
    fun updateSoundActive(active: Boolean) {
        soundActive = active
        sendControlCommand()
    }

    /**
     * 現在の状態に基づいてコントロールコマンドを送信します
     */
    private fun sendControlCommand() {
        viewModelScope.launch {
            val command = ControlCommand(
                senderId = senderId,
                direction = moveDirection,
                action = if (punishmentActive) "hammer_strike" else "none",
                sound = if (soundActive) "alarm" else "none"
            )
            firebaseService.sendControlCommand(targetUserId, command)
        }
    }

    /**
     * 操作を終了します
     */
    fun endControl() {
        viewModelScope.launch {
            callManager.endCall()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            callManager.endCall()
        }
    }
}
