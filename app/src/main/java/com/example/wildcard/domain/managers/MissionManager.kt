package com.example.wildcard.domain.managers

import com.example.wildcard.service.imagerecognition.ImageRecognitionService
import com.example.wildcard.service.firebase.FirebaseService
import com.example.wildcard.service.bluetooth.BluetoothService

/**
 * 起床ミッション管理マネージャー
 *
 * 起床ミッション（目の開閉認識）の開始、進捗管理、結果判定、
 * およびミッション成功時のユーザー状態更新とEV3アラーム停止を管理します。
 */
class MissionManager(
    private val imageRecognitionService: ImageRecognitionService,
    private val firebaseService: FirebaseService,
    private val bluetoothService: BluetoothService
) {

    /**
     * 起床ミッションを開始します。
     * @param userId ミッションを実行するユーザーのID
     * @param onProgress ミッションの進捗を通知するコールバック (0-100)
     * @param onResult ミッションの結果を通知するコールバック (true: 成功, false: 失敗)
     */
    suspend fun startStareMission(
        userId: String,
        onProgress: (Int) -> Unit,
        onResult: (Boolean) -> Unit
    ) {
        // TODO: FirebaseServiceを介してユーザーのステータスを「ミッション中」に更新
        // TODO: ImageRecognitionService を使用してカメラからの映像を処理し、目の開閉を検出
        // TODO: 10秒間目を開け続けるロジックを実装し、onProgressで進捗を通知
        // TODO: ミッション成功/失敗に応じてonResultを呼び出す

        val missionDurationMillis = 10000L // 10秒
        val startTime = System.currentTimeMillis()
        var eyesOpenDuration = 0L

        // 仮のミッションループ
        while (System.currentTimeMillis() - startTime < missionDurationMillis) {
            // TODO: imageRecognitionService.verifyEyesOpen() を定期的に呼び出す
            val eyesOpen = imageRecognitionService.verifyEyesOpen(100) // 仮の確率
            if (eyesOpen) {
                eyesOpenDuration += 100 // 仮のフレーム間隔
            }

            val progress = ((eyesOpenDuration.toFloat() / missionDurationMillis) * 100).toInt().coerceIn(0, 100)
            onProgress(progress)

            kotlinx.coroutines.delay(100) // 100msごとにチェック
        }

        val success = eyesOpenDuration >= missionDurationMillis // 10秒間目を開け続けたか

        if (success) {
            // TODO: FirebaseServiceを介してユーザーのステータスを「起床済み」に更新
            // TODO: BluetoothServiceを介してEV3にアラーム停止コマンドを送信
            bluetoothService.stopAlarm() // 仮
        }
        onResult(success)
    }
}
