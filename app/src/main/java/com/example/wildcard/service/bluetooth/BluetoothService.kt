package com.example.wildcard.service.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetoothサービス
 *
 * Android Bluetooth APIを使用して、EV3ロボットとのペアリング、接続、
 * およびデータの送受信を管理します。
 */
class BluetoothService(
    private val bluetoothAdapter: BluetoothAdapter?
) {

    // EV3のSPP (Serial Port Profile) UUID
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var connectedSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    /**
     * 指定されたMACアドレスのEV3デバイスに接続します。
     * @param deviceAddress EV3デバイスのMACアドレス
     * @return 接続に成功した場合はtrue、失敗した場合はfalse
     */
    fun connectToEv3(deviceAddress: String): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Bluetoothが利用できないか、有効になっていません
            return false
        }

        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)
        if (device == null) {
            // デバイスが見つかりません
            return false
        }

        try {
            connectedSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery() // 接続前にデバイス検索をキャンセル
            connectedSocket?.connect()
            inputStream = connectedSocket?.inputStream
            outputStream = connectedSocket?.outputStream
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            closeConnection()
            return false
        }
    }

    /**
     * EV3にアラーム再生コマンドを送信します。
     */
    fun triggerAlarm() {
        // TODO: EV3にアラーム再生を指示するコマンドを送信
        write("ALARM_ON".toByteArray())
    }

    /**
     * EV3にアラーム停止コマンドを送信します。
     */
    fun stopAlarm() {
        // TODO: EV3にアラーム停止を指示するコマンドを送信
        write("ALARM_OFF".toByteArray())
    }

    /**
     * EV3に操作データを送信します。
     * @param data 送信するバイト配列データ
     */
    fun write(data: ByteArray) {
        try {
            outputStream?.write(data)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * EV3からデータを受信します。
     * @return 受信したバイト配列データ、またはnull
     */
    fun read(): ByteArray? {
        val buffer = ByteArray(1024)
        var bytes: Int
        try {
            bytes = inputStream?.read(buffer) ?: -1
            if (bytes > 0) {
                return buffer.copyOf(bytes)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Bluetooth接続を閉じます。
     */
    fun closeConnection() {
        try {
            inputStream?.close()
            outputStream?.close()
            connectedSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream = null
            outputStream = null
            connectedSocket = null
        }
    }
}
