package com.example.wildcard.data.model

/**
 * ルームデータモデル
 *
 * Firebase Firestoreの`rooms`コレクションに保存されるルーム情報を表します。
 * @property roomCode ルームの合言葉（ドキュメントIDとして使用）
 * @property wakeupTime ルームで設定された起床時間 (Unix時間ミリ秒)
 * @property createdAt ルームが最初に作成された日時 (Unix時間ミリ秒)
 */
data class Room(
    val roomCode: String = "",
    val wakeupTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
