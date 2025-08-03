package com.example.wildcard.domain.managers

import android.content.Context
import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.webrtc.CallManager
import com.example.wildcard.service.bluetooth.BluetoothService
import com.google.firebase.auth.FirebaseAuth
import com.ntt.skyway.core.content.remote.RemoteVideoStream

/**
 * お仕置き機能管理マネージャー
 *
 * 未起床者へのEV3遠隔操作機能の開始、映像ストリーミング（映像のみ）の確立、
 * および操作コマンドの送信を管理します。
 */
class PunishmentManager(
    private val firebaseService: FirebaseService,
    private val applicationContext: Context,
    private val bluetoothService: BluetoothService
) {
    private val callManager = CallManager(applicationContext)

    /**
     * お仕置きを開始します（視聴側）。
     * 対象ユーザーのEV3搭載スマホとのWebRTC接続を確立し、映像ストリーミング（映像のみ）を開始します。
     * @param targetUserId お仕置き対象のユーザーID
     * @param onVideoStreamReady 映像ストリームの準備ができたときに呼び出されるコールバック
     */
    suspend fun startPunishment(
        targetUserId: String,
        onVideoStreamReady: (RemoteVideoStream?) -> Unit
    ) {
        val roomName = "control_$targetUserId"
        val success = callManager.startViewing(roomName)
        
        if (success) {
            // RemoteVideoStreamが準備できたときにコールバックを呼び出し
            onVideoStreamReady(callManager.remoteVideoStream)
        } else {
            onVideoStreamReady(null)
        }
    }

    /**
     * EV3への操作コマンドを送信します。
     * @param targetUserId 操作対象のユーザーID
     * @param controlData 送信する操作データ（例: "forward", "backward", "punish"）
     */
    suspend fun sendControlCommand(targetUserId: String, controlData: String) {
        // 送信者のIDを取得
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_sender"

        // 新しいコマンド形式(ControlCommand)に変換する
        val command = if (controlData == "punish") {
            ControlCommand(senderId = senderId, action = "hammer_strike")
        } else {
            ControlCommand(senderId = senderId, direction = controlData)
        }

        // コマンドを送信
        firebaseService.sendControlCommand(targetUserId, command)
    }

    /**
     * EV3に特定のアクションを実行させるコマンドを送信します。
     * @param targetUserId 操作対象のユーザーID
     */
    suspend fun triggerPunishAction(targetUserId: String) {
        sendControlCommand(targetUserId, "punish")
    }

    /**
     * お仕置きを終了します。
     */
    suspend fun endPunishment() {
        callManager.endCall()
    }
}
