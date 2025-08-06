package com.example.wildcard.utils

/**
 * アプリケーション全体で使用する定数を定義します。
 */
object Constants {
    const val ROOM_COLLECTION = "rooms"
    const val USERS_COLLECTION = "users"
    const val SIGNALING_COLLECTION = "signaling"
    const val COMMANDS_COLLECTION = "commands"

    // ユーザーのステータス
    const val STATUS_WAITING = "waiting"
    const val STATUS_MISSION = "mission"
    const val STATUS_WOKE_UP = "woke_up"

    // EV3 Bluetooth関連
    const val EV3_MAC_ADDRESS = "YOUR_EV3_MAC_ADDRESS_HERE" // TODO: 実際のEV3のMACアドレスに置き換える (例: 00:16:53:42:2B:99)
    
    // SkyWay WebRTC設定
    const val SKYWAY_APP_ID = "YOUR_SKYWAY_APP_ID_HERE" // TODO: SkyWay Consoleから取得したアプリIDを入力
    const val SKYWAY_SECRET_KEY = "YOUR_SKYWAY_SECRET_KEY_HERE" // TODO: SkyWay Consoleから取得したシークレットキーを入力
}
