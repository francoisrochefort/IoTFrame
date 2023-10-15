package com.etrak.scaleusb.api.mc

import android.app.PendingIntent
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class HardwareDevice(private val context: Context) : Device {

    private val usbSerialPort: UsbSerialPort by lazy {

        // Find all available drivers from attached devices.
        val usbManager = context.getSystemService(USB_SERVICE) as UsbManager?
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            throw McService.NoUsbDriverAvailableException()
        }

        // Open a connection to the first available driver.
        val usbSerialDriver = availableDrivers.first()
        val usbDeviceConnection = usbManager!!.openDevice(usbSerialDriver.device)

        // Request permission
        if (usbDeviceConnection == null) {
            while (!usbManager.hasPermission(usbSerialDriver.device)) {
                usbManager.requestPermission(
                    usbSerialDriver.device,
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
        val usbSerialPort = usbSerialDriver.ports.first() // Most devices have just one port (port 0)
        usbSerialPort.open(usbDeviceConnection)
        usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        usbSerialPort
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val messages = callbackFlow {

        // Register the listener
        val serialInputOutputManager = SerialInputOutputManager(
            usbSerialPort,
            object : SerialInputOutputManager.Listener {
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

                        // Extract the message from the buffer
                        val msg = buffer.substring(lt + 1, gt)

                        // Extract the code from the message
                        val code = msg.take(4)

                        // Extract parameters from the message
                        val params = msg.drop(4).split(',')

                        // Broadcast the message
                        trySend(
                            Device.Message(
                                code = code,
                                params = params
                            )
                        )

                        // Remove the message from the buffer
                        buffer = buffer.drop(gt - lt + 1)
                    }
                }
                override fun onRunError(e: Exception?) {
                }
            }
        )
        serialInputOutputManager.start()

        awaitClose {

            // Unregister the listener
            serialInputOutputManager.listener = null
            serialInputOutputManager.stop()
        }
    }

    override fun send(msg: Device.Message) {

        val src = "<${msg.code}${msg.params.joinToString(separator = ",")}>"
        usbSerialPort.write(src.toByteArray(), 100)
    }

    override fun connect() {
        // Everything is done just in time
    }
}