package com.etrak.scaleusb.api.mc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class Mc(

    private val context: Context

) {
    enum class ConnectionStatus {
        Connected,
        Disconnected
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED ->
                    _connectionStatus.value = ConnectionStatus.Connected
                UsbManager.ACTION_USB_DEVICE_DETACHED ->
                    _connectionStatus.value = ConnectionStatus.Disconnected
            }
        }
    }

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

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
        _connectionStatus.flatMapLatest { connectionStatus ->
            mode = when (connectionStatus) {
                ConnectionStatus.Connected -> Hardware(context)
                ConnectionStatus.Disconnected -> Software()
            }
            mode.messages
        }
    }

    fun send(msg: Mode.Message) = mode.send(msg)
}




















