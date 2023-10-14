package com.etrak.scaleusb.api.mc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.app.Service
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import com.etrak.scaleusb.R
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager

class McService : Service() {

    /**********************************************************************************************
     * Constants
     *********************************************************************************************/
    enum class Action {
        Start,
        Print,
        Stop
    }

    companion object {
        const val ON_MESSAGE = "com.example.ON_MESSAGE"
        const val EXTRA_MESSAGE_CODE = "com.example.EXTRA_MESSAGE_CODE"
        const val EXTRA_MESSAGE_PARAMS = "com.example.EXTRA_MESSAGE_PARAMS"
        const val CHANNEL_ID = "connection_status"
        const val NOTIFICATION_ID = 1
    }

    class NoUsbDriverAvailableException : Exception()

    /**********************************************************************************************
     * Variables
     *********************************************************************************************/
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var serialInputOutputManager: SerialInputOutputManager

    /**********************************************************************************************
     * Helpers
     *********************************************************************************************/
    private val builder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.usb)
            .setContentTitle(getString(R.string.content_title))
            .setContentText(getString(R.string.content_text))
    }

    private fun updateNotification(contentText: String) {
        builder.setContentText(contentText)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    private val listener = object : SerialInputOutputManager.Listener {
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

                // Extract the expression from the buffer
                val expr = buffer.substring(lt + 1, gt)

                // Extract the code from the expression
                val code = expr.take(4)

                // Extract parameters from the expression
                val params = expr.drop(4).split(',').toTypedArray()

                // Broadcast the command
                sendBroadcast(
                    Intent(ON_MESSAGE).apply {
                        putExtra(EXTRA_MESSAGE_CODE, code)
                        putExtra(EXTRA_MESSAGE_PARAMS, params)
                    }
                )

                // Remove the command expression from the buffer
                buffer = buffer.drop(gt - lt + 1)
            }
        }
        override fun onRunError(e: Exception?) {
        }
    }

    /**********************************************************************************************
     * Handlers
     *********************************************************************************************/
    private fun onStart() {

        // Find all available drivers from attached devices.
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager?
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            throw NoUsbDriverAvailableException()
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
                        this,
                        0,
                        Intent("com.eco_trak.balance.USB_PERMISSION"),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                Thread.sleep(5000)
            }
        }
        usbSerialPort = usbSerialDriver.ports.first() // Most devices have just one port (port 0)
        usbSerialPort.open(usbDeviceConnection)
        usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        serialInputOutputManager = SerialInputOutputManager(usbSerialPort, listener)
        serialInputOutputManager.start()

        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Init. the service
        startForeground(1, builder.build())
    }

    private fun onPrint(code: String, params: Array<String>) {
        val src = "<$code${params.joinToString(separator = ",")}>"
        usbSerialPort.write(src.toByteArray(), 100)
    }

    private fun onStop() {
        stopForeground(true)
        stopSelf()
    }

    /**********************************************************************************************
     * Service onStartCommand function
     *********************************************************************************************/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            Action.Start.name -> onStart()
            Action.Print.name -> onPrint(
                intent.getStringExtra(EXTRA_MESSAGE_CODE)!!,
                intent.getStringArrayExtra(EXTRA_MESSAGE_PARAMS)!!
            )
            Action.Stop.name -> onStop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**********************************************************************************************
     * Service onBind function
     *********************************************************************************************/
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}