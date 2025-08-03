package com.example.wildcard.data

import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebaseとのデータ送受信を担当するリポジトリ
 */
class FirebaseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // ユーザーを匿名認証で一意に識別

    /**
     * 指定された合言葉でルームに参加または新規作成する
     * @param username ユーザー名
     * @param roomCode ルームの合言葉
     */
    suspend fun joinOrCreateRoom(username: String, roomCode: String): Result<Unit> {
        return try {
            // 匿名認証でユーザーを一意に識別する (UIDを取得)
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            val uid = auth.currentUser!!.uid

            val roomRef = firestore.collection("rooms").document(roomCode)
            val userRef = firestore.collection("users").document(uid)

            firestore.runTransaction { transaction ->
                val roomSnapshot = transaction.get(roomRef)

                // ルームが存在しない場合、新しいルームを作成
                if (!roomSnapshot.exists()) {
                    val newRoom = Room(
                        roomCode = roomCode,
                        createdAt = System.currentTimeMillis()
                    )
                    transaction.set(roomRef, newRoom)
                }

                // ユーザー情報を作成または更新
                val newUser = User(
                    uid = uid,
                    username = username,
                    roomId = roomCode,
                    status = "waiting",
                    lastSeen = System.currentTimeMillis()
                )
                transaction.set(userRef, newUser)

                null // トランザクション成功
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ↓↓↓↓↓↓ ここからが新しく追加する部分です ↓↓↓↓↓↓

    /**
     * 現在ログインしているユーザーの情報を取得する
     */
    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            firestore.collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 指定したルームの情報をリアルタイムで監視する
     * @param roomId 監視対象のルームID
     * @return ルーム情報のFlow
     */
    fun listenToRoomUpdates(roomId: String): Flow<Room> = callbackFlow {
        val listener = firestore.collection("rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // エラーが発生したらFlowを閉じる
                    return@addSnapshotListener
                }
                snapshot?.toObject(Room::class.java)?.let { trySend(it) }
            }
        awaitClose { listener.remove() } // Flowがキャンセルされたらリスナーを解除
    }

    /**
     * 指定したルームに参加しているユーザーリストをリアルタイムで監視する
     * @param roomId 監視対象のルームID
     * @return ユーザーリストのFlow
     */
    fun listenToUsersInRoom(roomId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users").whereEqualTo("roomId", roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.toObjects(User::class.java)?.let { trySend(it) }
            }
        awaitClose { listener.remove() }
    }

    /**
     * ルームの起床時間を更新する
     * @param roomId 更新対象のルームID
     * @param newTime 新しい起床時間 (Unix時間ミリ秒)
     */
    suspend fun updateWakeupTime(roomId: String, newTime: Long) {
        firestore.collection("rooms").document(roomId).update("wakeupTime", newTime).await()
    }

    // ↑↑↑↑↑↑ ここまでが新しく追加する部分です ↑↑↑↑↑↑
}