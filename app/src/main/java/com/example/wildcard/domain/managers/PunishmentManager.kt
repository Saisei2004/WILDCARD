package com.example.wildcard.domain.managers

import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.webrtc.CallManager
import com.example.wildcard.service.bluetooth.BluetoothService

/**
 * お仕置き機能管理マネージャー
 *
 * 未起床者へのEV3遠隔操作機能の開始、映像ストリーミングの確立、
 * および操作コマンドの送信を管理します。
 */
class PunishmentManager(
    private val firebaseService: FirebaseService,
    private val callManager: CallManager,
    private val bluetoothService: BluetoothService
) {

    /**
     * お仕置きを開始します。
     * 対象ユーザーのEV3搭載スマホとのWebRTC接続を確立し、映像ストリーミングを開始します。
     * @param targetUserId お仕置き対象のユーザーID
     * @param onVideoStreamReady 映像ストリームの準備ができたときに呼び出されるコールバック
     */
    suspend fun startPunishment(
        targetUserId: String,
        onVideoStreamReady: (Any) -> Unit // TODO: 適切な映像ストリームオブジェクトの型に修正
    ) {
        // TODO: FirebaseServiceを介してシグナリング情報を交換し、WebRTC接続を確立
        // TODO: CallManager.startOneWayVideoStream() を呼び出し、映像ストリームを開始
        callManager.startOneWayVideoStream(targetUserId) // 仮
        onVideoStreamReady(Any()) // 仮
    }

    /**
     * EV3への操作コマンドを送信します。
     * @param targetUserId 操作対象のユーザーID
     * @param controlData 送信する操作データ（例: "forward", "backward", "left", "right", "punish"）
     */
    suspend fun sendControlCommand(targetUserId: String, controlData: String) {
        // TODO: FirebaseServiceを介して対象ユーザーのスマホに操作データを送信
        firebaseService.sendPunishmentCommand(targetUserId, controlData)
    }

    /**
     * EV3に特定のアクションを実行させるコマンドを送信します。
     * @param targetUserId 操作対象のユーザーID
     */
    suspend fun triggerPunishAction(targetUserId: String) {
        sendControlCommand(targetUserId, "punish")
    }
}
