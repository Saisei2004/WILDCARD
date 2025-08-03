package com.example.wildcard.domain.managers

import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.webrtc.CallManager
import com.example.wildcard.service.bluetooth.BluetoothService
import com.google.firebase.auth.FirebaseAuth

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
     * @param controlData 送信する操作データ（例: "forward", "backward", "punish"）
     */
    suspend fun sendControlCommand(targetUserId: String, controlData: String) {
        // --- ▼ ここからが修正部分 ▼ ---

        // 送信者のIDを取得
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_sender"

        // 新しいコマンド形式(ControlCommand)に変換する
        // controlDataが "punish" の場合はactionに、それ以外はdirectionにセットする
        val command = if (controlData == "punish") {
            ControlCommand(senderId = senderId, action = "hammer_strike")
        } else {
            ControlCommand(senderId = senderId, direction = controlData)
        }

        // 新しいメソッドを呼び出す
        firebaseService.sendControlCommand(targetUserId, command)

        // --- ▲ ここまでが修正部分 ▲ ---
    }

    /**
     * EV3に特定のアクションを実行させるコマンドを送信します。
     * @param targetUserId 操作対象のユーザーID
     */
    suspend fun triggerPunishAction(targetUserId: String) {
        // この関数は変更なしで、上記のsendControlCommandを正しく呼び出します
        sendControlCommand(targetUserId, "punish")
    }
}
