package com.example.wildcard.service.firebase

import com.example.wildcard.data.model.ControlCommand
import com.example.wildcard.data.model.Room
import com.example.wildcard.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService(
    private val firestore: FirebaseFirestore
) {

    private val roomsCollection = firestore.collection("rooms")
    private val usersCollection = firestore.collection("users")
    private val commandsCollection = firestore.collection("commands")

    // ... (findOrCreateRoom, updateUserStatus, updateWakeupTime, listenToRoomUpdates, listenToUsersInRoom, sendControlCommand, listenToCommands は変更なし) ...
    // (既存のコードはそのままにしてください)

    /**
     * ユーザーのステータス（例: "woke_up"）のみを更新します。
     */
    suspend fun updateUserStatus(userId: String, status: String): Boolean {
        return try {
            usersCollection.document(userId).update("status", status).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- ▼ ここから下の関数を丸ごと追加してください ▼ ---

    /**
     * 新しいユーザーを作成、または既存のユーザー情報を丸ごと上書きします。
     * RoomManagerでのユーザー新規登録時に使用します。
     * @param user 登録または更新するUserオブジェクト
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun setUserProfile(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- ▲ ここまでを追加 ▲ ---


    suspend fun findOrCreateRoom(roomCode: String): Room? {
        val roomRef = roomsCollection.document(roomCode)
        val snapshot = roomRef.get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(Room::class.java)
        } else {
            val newRoom = Room(roomCode = roomCode, wakeupTime = System.currentTimeMillis() + 3600000)
            roomRef.set(newRoom).await()
            newRoom
        }
    }

    suspend fun updateWakeupTime(roomId: String, wakeupTime: Long): Boolean {
        return try {
            roomsCollection.document(roomId).update("wakeupTime", wakeupTime).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun listenToRoomUpdates(roomId: String): Flow<Room?> = callbackFlow {
        val docRef = roomsCollection.document(roomId)
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Room::class.java))
        }
        awaitClose { listener.remove() }
    }

    fun listenToUsersInRoom(roomId: String): Flow<List<User>> = callbackFlow {
        val query = usersCollection.whereEqualTo("roomId", roomId)
        val listener = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val users = snapshots?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun sendControlCommand(targetUserId: String, command: ControlCommand): Boolean {
        return try {
            commandsCollection.document(targetUserId).set(command).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun listenToCommands(userId: String): Flow<ControlCommand> = callbackFlow {
        val docRef = commandsCollection.document(userId)
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(ControlCommand::class.java)?.let { trySend(it) }
            } else {
                trySend(ControlCommand())
            }
        }
        awaitClose { listener.remove() }
    }
}
