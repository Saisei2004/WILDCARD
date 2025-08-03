package com.example.wildcard.service.ev3

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class Ev3Controller(private val serverIp: String = "172.18.28.64") {

    private val serverUrl = "http://$serverIp:5000"

    fun sendRequest(endpoint: String) {
        // ✅ このログが最終目標です
        Log.d("EV3_DEBUG", "Sending request to EV3: $serverUrl$endpoint")
        thread {
            try {
                val url = URL("$serverUrl$endpoint")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 500
                conn.readTimeout = 500
                conn.connect()
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                // エラーが出た場合もログに出力
                Log.e("EV3_DEBUG", "Failed to send request to EV3", e)
                e.printStackTrace()
            }
        }
    }
}

