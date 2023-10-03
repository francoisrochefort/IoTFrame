package com.etrak.scaleusb.domain.scale

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NormalMode(

    private val context: Context

) : Mode {

    class NoUsbDriverAvailableException : Exception()

    private val port: UsbSerialPort by lazy {

        // Find all available drivers from attached devices.
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            throw NoUsbDriverAvailableException()
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers.first()
        val connection = manager!!.openDevice(driver.device)

        // Request permission
        if (connection == null) {
            while (!manager.hasPermission(driver.device)) {
                manager.requestPermission(
                    driver.device,
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent("com.eco_trak.balance.USB_PERMISSION"),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                Thread.sleep(5000)
            }
        }

        val port = driver.ports.first() // Most devices have just one port (port 0)

        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        // Return the port
        port
    }

    private lateinit var manager: SerialInputOutputManager

    @OptIn(ExperimentalCoroutinesApi::class)
    override val events: Flow<Scale.Event> by lazy {
        callbackFlow {

            val listener = object : SerialInputOutputManager.Listener {

                var buffer = ""

                override fun onNewData(data: ByteArray?) {
                    if (data != null) {

                        // Append new data to the buffer
                        buffer += String(data)

                        // Find the index of the opening delimiter
                        val lt = buffer.indexOf('<')
                        if (lt == -1) {
                            buffer = ""
                            return
                        }

                        // Find the index of the closing delimiter
                        val gt = buffer.indexOf('>', lt + 1)
                        if (gt == -1) {
                            return
                        }

                        // Extract the command expression from the buffer
                        val expr = buffer.substring(lt + 1, gt)

                        // Extract the command from the command expression
                        val cmd = expr.take(4)

                        // Extract command parameters from the command expression
                        val params = expr.drop(4).split(',')

                        // Broadcast the command
                        when (cmd) {
                            "AD38" -> {
                                trySend(Scale.Event.OnCabAngle(params[0].toInt()))
                            }
                        }

                        // Remove the command expression from the buffer
                        buffer = buffer.drop(gt - lt + 1)
                    }
                }

                override fun onRunError(e: Exception?) {
                    trySend(Scale.Event.OnError(e))
                }
            }

            manager = SerialInputOutputManager(port, listener)
            manager.start()

            awaitClose {
            }
        }
    }

    override fun print(message: String) {
        port.write(message.toByteArray(), 100)
    }
}