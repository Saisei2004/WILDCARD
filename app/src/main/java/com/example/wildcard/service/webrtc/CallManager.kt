package com.example.wildcard.service.webrtc

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.wildcard.utils.Constants
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalVideoStream
import com.ntt.skyway.core.content.local.source.CameraSource
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * SkyWayベースのWebRTC通話管理マネージャー
 *
 * SkyWayを使用してWebRTC接続の確立、映像ストリーミング（映像のみ）の開始・停止を管理します。
 */
class CallManager(
    private val applicationContext: Context
) {
    // SkyWayContext.Optionsの設定
    private val option = SkyWayContext.Options(
        logLevel = Logger.LogLevel.VERBOSE
    )

    // SkyWayの認証・認可に利用するIDとキー
    private val appId = Constants.SKYWAY_APP_ID
    private val secretKey = Constants.SKYWAY_SECRET_KEY

    // SkyWayの各種オブジェクトを保持する変数
    private var localRoomMember: LocalRoomMember? = null
    private var room: P2PRoom? = null

    // コルーチンスコープ
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 状態管理
    var localVideoStream by mutableStateOf<LocalVideoStream?>(null)
        private set
    var remoteVideoStream by mutableStateOf<RemoteVideoStream?>(null)
        private set

    /**
     * 配信者として映像ストリーム（映像のみ）を開始します。
     * @param roomName 参加するルーム名
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun startPublishing(roomName: String): Boolean {
        return try {
            val result = SkyWayContext.setupForDev(applicationContext, appId, secretKey, option)
            if (!result) {
                Log.e("CallManager", "SkyWay setup failed")
                return false
            }

            // カメラの設定と開始
            val device = CameraSource.getBackCameras(applicationContext).firstOrNull()
            if (device == null) {
                Log.e("CallManager", "No back camera found")
                return false
            }

            val cameraOption = CameraSource.CapturingOptions(160, 160, 5)
            CameraSource.startCapturing(applicationContext, device, cameraOption)
            
            withContext(Dispatchers.Main) {
                localVideoStream = CameraSource.createStream()
            }

            // ルームに参加
            room = P2PRoom.findOrCreate(name = roomName)
            val memberInit = RoomMember.Init(name = "publisher_${UUID.randomUUID()}")
            localRoomMember = room?.join(memberInit)

            // 映像を公開
            localVideoStream?.let { stream ->
                localRoomMember?.publish(stream)
            }

            Log.d("CallManager", "Publishing started successfully")
            true
        } catch (e: Exception) {
            Log.e("CallManager", "Failed to start publishing", e)
            false
        }
    }

    /**
     * 視聴者として映像ストリーム（映像のみ）を受信します。
     * @param roomName 参加するルーム名
     * @return 成功した場合はtrue、失敗した場合はfalse
     */
    suspend fun startViewing(roomName: String): Boolean {
        return try {
            val result = SkyWayContext.setupForDev(applicationContext, appId, secretKey, option)
            if (!result) {
                Log.e("CallManager", "SkyWay setup failed")
                return false
            }

            // ルームに参加
            room = P2PRoom.findOrCreate(name = roomName)
            val memberInit = RoomMember.Init(name = "viewer_${UUID.randomUUID()}")
            localRoomMember = room?.join(memberInit)

            // 新しいストリームが公開された時のハンドラ
            room?.onStreamPublishedHandler = { publication ->
                if (publication.publisher?.id != localRoomMember?.id) {
                    scope.launch {
                        subscribe(publication)
                    }
                }
            }

            // 既に公開されているストリームを購読
            room?.publications?.forEach { publication ->
                if (publication.publisher?.id != localRoomMember?.id) {
                    scope.launch {
                        subscribe(publication)
                    }
                }
            }

            Log.d("CallManager", "Viewing started successfully")
            true
        } catch (e: Exception) {
            Log.e("CallManager", "Failed to start viewing", e)
            false
        }
    }

    /**
     * ストリームを購読する
     */
    private suspend fun subscribe(publication: RoomPublication) {
        try {
            val subscription = localRoomMember?.subscribe(publication)
            subscription?.stream?.let { stream ->
                if (stream.contentType == Stream.ContentType.VIDEO) {
                    withContext(Dispatchers.Main) {
                        remoteVideoStream = stream as RemoteVideoStream
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallManager", "Failed to subscribe to stream", e)
        }
    }

    /**
     * WebRTC接続を終了します。
     */
    suspend fun endCall() {
        try {
            val roomToLeave = room ?: return
            val memberToLeave = localRoomMember ?: return

            // 公開しているストリームを停止
            memberToLeave.publications.forEach {
                memberToLeave.unpublish(it)
            }

            // ルームから退出
            memberToLeave.leave()

            // 状態をリセット
            withContext(Dispatchers.Main) {
                localVideoStream = null
                remoteVideoStream = null
            }

            // リソースを解放
            roomToLeave.dispose()
            room = null
            localRoomMember = null

            // コルーチンスコープをキャンセル
            scope.cancel()

            Log.d("CallManager", "Call ended successfully")
        } catch (e: Exception) {
            Log.e("CallManager", "Failed to end call", e)
        }
    }
}
