package com.example.wildcard.data.model

data class ControlCommand(
    val senderId: String = "",      // 送信者のユーザーID
    val direction: String = "stop", // "forward", "backward", "left", "right", "stop"
    val action: String = "none",     // "hammer_strike", "none"
    val sound: String = "none"      // ✅ "alarm" (音を鳴らす), "none" (鳴らさない) を追加
)
