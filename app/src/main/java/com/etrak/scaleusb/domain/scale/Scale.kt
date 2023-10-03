package com.etrak.scaleusb.domain.scale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.Exception

const val TAG = "eco-trak"

class Scale(

    private val context: Context
) {

    sealed class Event {
        data class OnCabAngle(val angle: Int) : Event()
        data class OnError(val e: Exception?) : Event()
    }

    enum class ConnectionStatus {
        Connected,
        Disconnected
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
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

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

        val receiver = Receiver()
        context.registerReceiver(receiver, filter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val events by lazy {
        connectionStatus.flatMapLatest { connectionStatus ->
            when (connectionStatus) {
                ConnectionStatus.Connected -> {
                    mode = NormalMode(context)
                    mode.events
                }
                ConnectionStatus.Disconnected ->  {
                    mode = DemoMode()
                    mode.events
                }
            }
        }
    }
}




















