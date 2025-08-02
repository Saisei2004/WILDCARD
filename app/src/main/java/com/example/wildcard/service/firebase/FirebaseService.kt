package com.example.wildcard.service.firebase

import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Firebaseサービス
 *
 * Firebase Firestoreとのデータ連携、ユーザー認証、リアルタイムリスナーの設定など、
 * Firebase関連の処理を一元的に管理します。
 */
class FirebaseService(
    private val firestore: FirebaseFirestore
) {

    private val roomsCollection = firestore.collection("rooms")
    private val usersCollection = firestore.collection("users")

    /**
     * 指定されたルームを検索し、存在しない場合は新規作成します。
     * @param roomCode ルームの合言葉
     * @return 検索または作成されたRoomオブジェクト
     */
    suspend fun findOrCreateRoom(roomCode: String): Room? {
        val roomRef = roomsCollection.document(roomCode)
        val snapshot = roomRef.get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(Room::class.java)
        } else {
            val newRoom = Room(roomCode = roomCode, wakeupTime = System.currentTimeMillis() + 3600000) // 仮で1時間後に設定
            roomRef.set(newRoom).await()
            newRoom
        }
    }

    /**
     * ユーザーのステータスを更新します。
     * @param user 更新するUserオブジェクト
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun updateUserStatus(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * ルームの設定（起床時間など）を更新します。
     * @param roomCode 更新対象のルーム合言葉
     * @param wakeupTime 新しい起床時間 (Unix時間ミリ秒)
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun updateRoomSettings(roomCode: String, wakeupTime: Long): Boolean {
        return try {
            roomsCollection.document(roomCode).update("wakeupTime", wakeupTime).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * ルームの状態変更をリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onRoomStateChanged ルームの状態が変更されたときに呼び出されるコールバック
     * @return ListenerRegistration オブジェクト。リスナーを解除するために使用します。
     */
    fun listenToRoomState(roomCode: String, onRoomStateChanged: (Room) -> Unit): ListenerRegistration {
        return roomsCollection.document(roomCode)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(Room::class.java)?.let { onRoomStateChanged(it) }
                }
            }
    }

    /**
     * ルーム内の全ユーザーのステータスをリアルタイムで監視します。
     * @param roomCode 監視対象のルーム合言葉
     * @param onUsersStatusChanged ユーザーのステータスが変更されたときに呼び出されるコールバック
     * @return ListenerRegistration オブジェクト。リスナーを解除するために使用します。
     */
    fun listenToUsersStatus(roomCode: String, onUsersStatusChanged: (List<User>) -> Unit): ListenerRegistration {
        return usersCollection.whereEqualTo("roomId", roomCode)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val users = snapshots.mapNotNull { it.toObject(User::class.java) }
                    onUsersStatusChanged(users)
                }
            }
    }

    /**
     * WebRTCシグナリングメッセージを送信します。
     * @param targetUserId 送信先のユーザーID
     * @param message シグナリングメッセージ（SDP, ICE Candidateなど）
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun sendSignalingMessage(targetUserId: String, message: Map<String, Any>): Boolean {
        // TODO: Firebase Realtime Database または Firestore を使用してシグナリングメッセージを送信
        // 例: firestore.collection("signaling").document(targetUserId).collection("messages").add(message).await()
        return true // 仮
    }

    /**
     * お仕置き操作コマンドを送信します。
     * @param targetUserId 送信先のユーザーID
     * @param controlData 操作データ
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun sendPunishmentCommand(targetUserId: String, controlData: String): Boolean {
        // TODO: Firebase Realtime Database または Firestore を使用して操作コマンドを送信
        // 例: firestore.collection("commands").document(targetUserId).set(mapOf("command" to controlData)).await()
        return true // 仮
    }
}
