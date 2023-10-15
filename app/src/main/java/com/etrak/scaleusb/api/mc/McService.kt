package com.etrak.scaleusb.api.mc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.etrak.scaleusb.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class McService(

    private val emulator: Device

) : LifecycleService() {

    companion object {

        // Intents
        const val ON_MESSAGE = "com.example.ON_MESSAGE"
        const val EXTRA_MESSAGE_CODE = "com.example.EXTRA_MESSAGE_CODE"
        const val EXTRA_MESSAGE_PARAMS = "com.example.EXTRA_MESSAGE_PARAMS"

        const val CHANNEL_ID = "connection_status"
        const val NOTIFICATION_ID = 1
    }

    // Intent actions
    enum class Action {
        Start,
        Send,
        Stop
    }

    enum class ConnectionStatus {
        Connected,
        Disconnected
    }

    class NoUsbDriverAvailableException : Exception()

    // When a device is attached or detached then change the connection status
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

    private val receiver = Receiver()
    private val connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    private lateinit var device: Device

    // When the connection status changes then switch between flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = connectionStatus.flatMapLatest { connectionStatus ->
        device = when (connectionStatus) {
            ConnectionStatus.Connected -> HardwareDevice(applicationContext)
            ConnectionStatus.Disconnected -> emulator
        }
        device.messages
    }
    .shareIn(lifecycleScope, SharingStarted.Eagerly)

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

    private fun onStart() {

        // Collect message from devices and broadcast them
        lifecycleScope.launch {
            messages.collect { msg ->
                sendBroadcast(
                    Intent(ON_MESSAGE).apply {
                        putExtra(EXTRA_MESSAGE_CODE, msg.code)
                        putExtra(EXTRA_MESSAGE_PARAMS, msg.params.toTypedArray())
                    }
                )
            }
        }

        // Register the receiver
        IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            registerReceiver(receiver, this)
        }

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

    private fun onPrint(msg: Device.Message) {
        device.send(msg)
    }

    private fun onStop() {
        unregisterReceiver(receiver)
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Dispatch the action to its handler
        when (intent?.action) {
            Action.Start.name -> onStart()
            Action.Send.name -> onPrint(
                Device.Message(
                    intent.getStringExtra(EXTRA_MESSAGE_CODE)!!,
                    intent.getStringArrayExtra(EXTRA_MESSAGE_PARAMS)!!.toList()
                )
            )
            Action.Stop.name -> onStop()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}