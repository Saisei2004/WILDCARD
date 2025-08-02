package com.example.wildcard.domain.managers

import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.example.wildcard.service.firebase.FirebaseService

/**
 * ルーム管理マネージャー
 *
 * ユーザーの登録、ルームの作成・参加、ルームの状態更新など、
 * ルームに関するビジネスロジックを管理します。
 * FirebaseServiceと連携してデータの永続化を行います。
 */
class RoomManager(
    private val firebaseService: FirebaseService
) {

    /**
     * ユーザーを登録し、指定されたルームに参加または作成します。
     * @param username ユーザー名
     * @param roomCode ルームの合言葉
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun registerUserAndJoinOrCreateRoom(username: String, roomCode: String): Boolean {
        // TODO: Firebase Authenticationでユーザーを認証し、UIDを取得
        val uid = "dummy_uid_" + System.currentTimeMillis() // 仮のUID

        // ルームを検索または作成
        val room = firebaseService.findOrCreateRoom(roomCode)
        if (room == null) {
            // ルームの作成に失敗した場合
            return false
        }

        // ユーザー情報をFirebaseに保存
        val user = User(uid = uid, username = username, roomId = roomCode, status = "waiting")
        return firebaseService.updateUserStatus(user)
    }

    /**
     * ルームの起床時間を更新します。
     * @param roomCode 更新対象のルーム合言葉
     * @param wakeupTime 新しい起床時間 (Unix時間ミリ秒)
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun updateWakeupTime(roomCode: String, wakeupTime: Long): Boolean {
        // TODO: FirebaseServiceを介してルームの起床時間を更新
        return firebaseService.updateRoomSettings(roomCode, wakeupTime)
    }

    /**
     * ルームの状態変更をリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onRoomStateChanged ルームの状態が変更されたときに呼び出されるコールバック
     */
    fun listenToRoomState(roomCode: String, onRoomStateChanged: (Room) -> Unit) {
        // TODO: FirebaseServiceを介してルームの状態変更をリッスン
        firebaseService.listenToRoomState(roomCode, onRoomStateChanged)
    }

    /**
     * ルーム内の全ユーザーのステータスをリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onUsersStatusChanged ユーザーのステータスが変更されたときに呼び出されるコールバック
     */
    fun listenToUsersStatus(roomCode: String, onUsersStatusChanged: (List<User>) -> Unit) {
        // TODO: FirebaseServiceを介してルーム内のユーザーのステータス変更をリッスン
        firebaseService.listenToUsersStatus(roomCode, onUsersStatusChanged)
    }
}
