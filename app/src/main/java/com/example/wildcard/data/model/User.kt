package com.example.wildcard.data.model

/**
 * ユーザーデータモデル
 *
 * Firebase Firestoreの`users`コレクションに保存されるユーザー情報を表します。
 * @property uid Firebase Authenticationなどで発行される一意のユーザーID
 * @property username ユーザーが設定した名前
 * @property roomId 現在参加しているルームのID（合言葉）
 * @property status ユーザーの現在の状態 (`waiting`, `mission`, `woke_up`)
 * @property lastSeen ユーザーの最終アクティブ日時（オフライン判定などに使用）
 */
data class User(
    val uid: String = "",
    val username: String = "",
    val roomId: String = "",
    val status: String = "waiting", // waiting, mission, woke_up
    val lastSeen: Long = System.currentTimeMillis()
)
