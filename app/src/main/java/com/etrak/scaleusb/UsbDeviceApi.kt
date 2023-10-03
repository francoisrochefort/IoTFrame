package com.etrak.scaleusb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager

class UsbDeviceApi private constructor(

    val port: UsbSerialPort

) {
    class Builder {

        class NoUsbDriverAvailableException : Exception()

        private var baudRate = 115200
        private var dataBits = 8
        private var stopBits = UsbSerialPort.STOPBITS_1
        private var parity = UsbSerialPort.PARITY_NONE

        lateinit var port: UsbSerialPort

        fun baudRate(baudRate: Int) = apply { this.baudRate = baudRate }
        fun dataBits(dataBits: Int) = apply { this.dataBits = dataBits }
        fun stopBits(stopBits: Int) = apply { this.stopBits = stopBits }
        fun parity(parity: Int) = apply { this.parity = parity }

        fun build(context: Context) : UsbDeviceApi {

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

            port = driver.ports.first() // Most devices have just one port (port 0)

            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            return UsbDeviceApi(this)
        }
    }

    constructor(builder: Builder) : this(
        builder.port
    )
}