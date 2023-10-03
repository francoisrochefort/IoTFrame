package com.etrak.scaleusb

import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.Exception

const val TAG = "eco-trak"

class Scale(

//    private val device: UsbDeviceApi
) {
    sealed class Event {
        data class OnCabAngle(val angle: Int) : Event()
        data class OnError(val e: Exception?) : Event()
    }

    enum class ConnectionStatus {
        Connected,
        Disconnected
    }

    private lateinit var manager: SerialInputOutputManager

    private val connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventFlow by lazy {
        connectionStatus.flatMapLatest { connectionStatus ->
            when (connectionStatus) {

                // Normal mode
                ConnectionStatus.Connected -> callbackFlow<Event> {

                    val listener = object : SerialInputOutputManager.Listener {

                        var buffer = ""

                        override fun onNewData(data: ByteArray?) {
                            if (data != null) {
                                buffer += String(data)
                                val lt = buffer.indexOf('<')
                                if (lt == -1) {
                                    buffer = ""
                                    return
                                }
                                val gt = buffer.indexOf('>', lt + 1)
                                if (gt == -1) {
                                    return
                                }
                                val expr = buffer.substring(lt + 1, gt)
                                val cmd = expr.take(4)
                                val params = expr.drop(4).split(',')
                                when (cmd) {
                                    "AD38" -> {
                                        trySend(Event.OnCabAngle(params[0].toInt()))
                                    }
                                }
                                buffer = buffer.drop(gt - lt + 1)
                            }
                        }

                        override fun onRunError(e: Exception?) {
                            trySend(Event.OnError(e))
                        }
                    }

//                    manager = SerialInputOutputManager(device.port, listener)
//                    manager.start()

                    awaitClose {
                        // TODO: Unregister the listener
                    }
                }

                // Demo mode
                ConnectionStatus.Disconnected -> flow<Event> {

                    var angle = 0
                    while (true) {
                        emit(Event.OnCabAngle(angle))
                        delay(1000)
                        angle++
                    }
                }

            }
        }
    }
}




















