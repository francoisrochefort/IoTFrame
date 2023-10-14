package com.etrak.scaleusb.api.mc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

const val TAG = "e-trak mc"

class Mc(

    private val context: Context

) {
    enum class ConnectionStatus {
        Connected,
        Disconnected
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Log.d(TAG, "Mc::onReceive")

            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED ->
                    connectionStatus.value = ConnectionStatus.Connected
                UsbManager.ACTION_USB_DEVICE_DETACHED ->
                    connectionStatus.value = ConnectionStatus.Disconnected
            }
        }
    }

    private val connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    private lateinit var mode: Mode

    fun connect() {
        IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            val receiver = Receiver()
            context.registerReceiver(receiver, this)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages by lazy {
        connectionStatus.flatMapLatest { connectionStatus ->
            mode = when (connectionStatus) {
                ConnectionStatus.Connected -> NormalMode(context)
                ConnectionStatus.Disconnected -> DemoMode()
            }
            mode.messages
        }
    }
}




















