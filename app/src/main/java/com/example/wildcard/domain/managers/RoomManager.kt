package com.example.wildcard.domain.managers

import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.example.wildcard.service.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth

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
        // 認証済みのユーザーのUIDを取得。なければ処理を中断。
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        // ルームを検索または作成
        val room = firebaseService.findOrCreateRoom(roomCode)
        if (room == null) {
            // ルームの作成に失敗した場合
            return false
        }

        // ユーザー情報をFirebaseに保存
        val user = User(uid = uid, username = username, roomId = roomCode, status = "waiting")

        // --- ▼ ここからが修正部分 ▼ ---
        // 新しいユーザーを丸ごと登録するメソッドを呼び出す
        return firebaseService.setUserProfile(user)
        // --- ▲ ここまでが修正部分 ▲ ---
    }

    /**
     * ルームの起床時間を更新します。
     * @param roomCode 更新対象のルーム合言葉
     * @param wakeupTime 新しい起床時間 (Unix時間ミリ秒)
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun updateWakeupTime(roomCode: String, wakeupTime: Long): Boolean {
        // この部分は変更なし
        return firebaseService.updateWakeupTime(roomCode, wakeupTime)
    }

    /**
     * ルームの状態変更をリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onRoomStateChanged ルームの状態が変更されたときに呼び出されるコールバック
     */
    fun listenToRoomState(roomCode: String, onRoomStateChanged: (Room?) -> Unit) {
        // TODO: FirebaseServiceを介してルームの状態変更をリッスン
        // この部分はViewModelでFlowを直接使うように変更したため、
        // このマネージャーからは直接使われない可能性があります。
        // 必要であればViewModelと同様にFlowを扱うように修正します。
    }

    /**
     * ルーム内の全ユーザーのステータスをリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onUsersStatusChanged ユーザーのステータスが変更されたときに呼び出されるコールバック
     */
    fun listenToUsersStatus(roomCode: String, onUsersStatusChanged: (List<User>) -> Unit) {
        // TODO: FirebaseServiceを介してルーム内のユーザーのステータス変更をリッスン
        // この部分はViewModelでFlowを直接使うように変更したため、
        // このマネージャーからは直接使われない可能性があります。
    }
}
